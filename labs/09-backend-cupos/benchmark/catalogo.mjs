import { spawn } from 'node:child_process';
import { once } from 'node:events';
import { Agent, get } from 'node:http';
import { setTimeout as sleep } from 'node:timers/promises';

const jar = 'target/backend-cupos-1.0.0-SNAPSHOT.jar';
const url = 'http://127.0.0.1:58081/cursos';
const rates = [25, 50, 100];
const agent = new Agent({ keepAlive: true, maxSockets: 24 });

function request() {
    const start = performance.now();
    return new Promise((resolve) => {
        const req = get(url, { agent, headers: { 'Accept-Encoding': 'gzip' } }, (res) => {
            let bytes = 0;
            res.on('data', (chunk) => { bytes += chunk.length; });
            res.on('end', () => resolve({
                ok: res.statusCode === 200 && res.complete,
                ms: performance.now() - start,
                bytes,
                encoding: res.headers['content-encoding'] ?? 'identity',
            }));
            res.on('error', () => resolve({ ok: false }));
        });
        req.on('error', () => resolve({ ok: false }));
        const timer = setTimeout(() => req.destroy(), 2000);
        req.on('close', () => clearTimeout(timer));
    });
}

function percentile(values, fraction) {
    if (!values.length) return null;
    const sorted = values.toSorted((a, b) => a - b);
    return +sorted[Math.ceil(fraction * sorted.length) - 1].toFixed(2);
}

async function sample(rate, seconds, encoding) {
    const start = performance.now();
    const pending = [];
    const delays = [];
    for (let i = 0; i < rate * seconds; i++) {
        const due = start + i * 1000 / rate;
        const delay = due - performance.now();
        if (delay >= 1) await sleep(delay);
        delays.push(Math.max(0, performance.now() - due));
        pending.push(request());
    }
    const results = await Promise.all(pending);
    const elapsed = Math.max(seconds, (performance.now() - start) / 1000);
    const successes = results.filter((r) => r.ok);
    if (successes.some((r) => r.encoding !== encoding)) throw new Error('Unexpected Content-Encoding');
    return {
        offered: results.length,
        successful: successes.length,
        completedRps: +(results.length / elapsed).toFixed(2),
        p95Ms: percentile(successes.map((r) => r.ms), 0.95),
        p95StartDelayMs: percentile(delays, 0.95),
        bodyBytes: successes[0]?.bytes ?? null,
    };
}

async function run(compressed, repetition) {
    const java = process.env.JAVA_HOME ? `${process.env.JAVA_HOME}/bin/java.exe` : 'java';
    const app = spawn(java, [
        '-Xms256m', '-Xmx512m', '-jar', jar, '--server.port=58081',
        '--spring.flyway.locations=classpath:db/migration,filesystem:benchmark/migration',
        `--server.compression.enabled=${compressed}`,
    ], { windowsHide: true, stdio: ['ignore', 'ignore', 'inherit'] });
    const closed = once(app, 'close');
    try {
        let ready = false;
        for (let i = 0; i < 60 && app.exitCode === null; i++) {
            if ((await request()).ok) {
                ready = true;
                break;
            }
            await sleep(250);
        }
        if (!ready) throw new Error('Application did not become ready');
        const encoding = compressed ? 'gzip' : 'identity';
        await sample(50, 3, encoding);
        for (const rate of rates) {
            console.log(JSON.stringify({ encoding, repetition, rate, seconds: 6, ...await sample(rate, 6, encoding) }));
        }
    } finally {
        app.kill();
        await closed;
    }
}

try {
    if (!process.env.CURSOS_JDBC_URL?.startsWith('jdbc:postgresql://127.0.0.1:55433/')) {
        throw new Error('Use the dedicated benchmark database on port 55433');
    }
    for (const [compressed, repetition] of [[false, 1], [true, 1], [true, 2], [false, 2]]) {
        await run(compressed, repetition);
    }
} finally {
    agent.destroy();
}
