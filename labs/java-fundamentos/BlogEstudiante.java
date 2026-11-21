import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BlogEstudiante {
    public static void main(String[] args) {
        Repositorio repo = new Repositorio();

        Perfil alumno = repo.crearPerfil("Alumno");
        Estatus activo = repo.crearEstatus("Activo");
        Usuario usuario = repo.crearUsuario("miguel", "Miguel Lopez", alumno, activo);

        Categoria programacion = repo.crearCategoria("Programacion");
        Tema java = repo.crearTema(programacion, "Java");

        BlogPost post = repo.crearBlog(usuario, java,
                "Primer blog de Java",
                "Java sigue siendo util para aprender POO, estructuras y aplicaciones web.");

        repo.comentar(usuario, post, "Buen tema para empezar.");
        repo.imprimirResumen();
    }

    private static class Repositorio {
        private int siguienteId = 1;
        private final Map<Integer, Perfil> perfiles = new LinkedHashMap<Integer, Perfil>();
        private final Map<Integer, Estatus> estatusUsuarios = new LinkedHashMap<Integer, Estatus>();
        private final Map<Integer, Usuario> usuarios = new LinkedHashMap<Integer, Usuario>();
        private final Map<Integer, Categoria> categorias = new LinkedHashMap<Integer, Categoria>();
        private final Map<Integer, Tema> temas = new LinkedHashMap<Integer, Tema>();
        private final Map<Integer, BlogPost> blogs = new LinkedHashMap<Integer, BlogPost>();

        Perfil crearPerfil(String nombre) {
            Perfil perfil = new Perfil(nuevoId(), nombre);
            perfiles.put(perfil.id, perfil);
            return perfil;
        }

        Estatus crearEstatus(String nombre) {
            Estatus estatus = new Estatus(nuevoId(), nombre);
            estatusUsuarios.put(estatus.id, estatus);
            return estatus;
        }

        Usuario crearUsuario(String alias, String nombre, Perfil perfil, Estatus estatus) {
            Usuario usuario = new Usuario(nuevoId(), alias, nombre, perfil, estatus);
            usuarios.put(usuario.id, usuario);
            return usuario;
        }

        Categoria crearCategoria(String nombre) {
            Categoria categoria = new Categoria(nuevoId(), nombre);
            categorias.put(categoria.id, categoria);
            return categoria;
        }

        Tema crearTema(Categoria categoria, String nombre) {
            Tema tema = new Tema(nuevoId(), categoria, nombre);
            temas.put(tema.id, tema);
            categoria.temas.add(tema);
            return tema;
        }

        BlogPost crearBlog(Usuario autor, Tema tema, String titulo, String contenido) {
            BlogPost blog = new BlogPost(nuevoId(), autor, tema, titulo, contenido, LocalDateTime.now());
            blogs.put(blog.id, blog);
            return blog;
        }

        Comentario comentar(Usuario autor, BlogPost blog, String contenido) {
            Comentario comentario = new Comentario(nuevoId(), autor, blog, contenido, LocalDateTime.now());
            blog.comentarios.add(comentario);
            return comentario;
        }

        void imprimirResumen() {
            System.out.println("BLOG DEL ESTUDIANTE");

            System.out.println("\nUsuarios:");
            for (Usuario usuario : usuarios.values()) {
                System.out.println(usuario);
            }

            System.out.println("\nCategorías y temas:");
            for (Categoria categoria : categorias.values()) {
                System.out.println("- " + categoria.nombre);
                for (Tema tema : categoria.temas) {
                    System.out.println("  * " + tema.nombre);
                }
            }

            System.out.println("\nBlogs:");
            for (BlogPost blog : blogs.values()) {
                System.out.println(blog);
                for (Comentario comentario : blog.comentarios) {
                    System.out.println("  Comentario de " + comentario.autor.alias + ": " + comentario.contenido);
                }
            }
        }

        private int nuevoId() {
            return siguienteId++;
        }
    }

    private static class Perfil {
        final int id;
        final String nombre;

        Perfil(int id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }
    }

    private static class Estatus {
        final int id;
        final String nombre;

        Estatus(int id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }
    }

    private static class Usuario {
        final int id;
        final String alias;
        final String nombre;
        final Perfil perfil;
        final Estatus estatus;

        Usuario(int id, String alias, String nombre, Perfil perfil, Estatus estatus) {
            this.id = id;
            this.alias = alias;
            this.nombre = nombre;
            this.perfil = perfil;
            this.estatus = estatus;
        }

        public String toString() {
            return alias + " (" + nombre + ") - perfil: " + perfil.nombre + ", estatus: " + estatus.nombre;
        }
    }

    private static class Categoria {
        final int id;
        final String nombre;
        final List<Tema> temas = new ArrayList<Tema>();

        Categoria(int id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }
    }

    private static class Tema {
        final int id;
        final Categoria categoria;
        final String nombre;

        Tema(int id, Categoria categoria, String nombre) {
            this.id = id;
            this.categoria = categoria;
            this.nombre = nombre;
        }
    }

    private static class BlogPost {
        final int id;
        final Usuario autor;
        final Tema tema;
        final String titulo;
        final String contenido;
        final LocalDateTime creado;
        final List<Comentario> comentarios = new ArrayList<Comentario>();

        BlogPost(int id, Usuario autor, Tema tema, String titulo, String contenido, LocalDateTime creado) {
            this.id = id;
            this.autor = autor;
            this.tema = tema;
            this.titulo = titulo;
            this.contenido = contenido;
            this.creado = creado;
        }

        public String toString() {
            return titulo + " | autor: " + autor.alias + " | categoria: " + tema.categoria.nombre
                    + " | tema: " + tema.nombre + "\n  " + contenido;
        }
    }

    private static class Comentario {
        final int id;
        final Usuario autor;
        final BlogPost blog;
        final String contenido;
        final LocalDateTime creado;

        Comentario(int id, Usuario autor, BlogPost blog, String contenido, LocalDateTime creado) {
            this.id = id;
            this.autor = autor;
            this.blog = blog;
            this.contenido = contenido;
            this.creado = creado;
        }
    }
}
