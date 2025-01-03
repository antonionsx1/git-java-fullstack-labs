package org.iadb.lmw.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.iadb.lmw.dao.CommentDao;
import org.iadb.lmw.dto.CommentsFileResponseDto;
import org.iadb.lmw.dto.CreateCommentRequestDto;
import org.iadb.lmw.dto.CreateCommentResponseDto;
import org.iadb.lmw.dto.DeleteCommentResponseDto;
import org.iadb.lmw.dto.DeleteCommentsRequestDto;
import org.iadb.lmw.dto.DownloadCommentsRequestDto;
import org.iadb.lmw.dto.ReadCommentResponseDto;
import org.iadb.lmw.dto.ReadCommentsRequestDto;
import org.iadb.lmw.dto.ViewCommentResponseDto;
import org.iadb.lmw.model.Comment;
import org.iadb.lmw.service.CommentService;
import org.iadb.lmw.service.DynamicSQLQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;

@Service
public class CommentServiceImpl implements CommentService {

	private static final Logger log = LoggerFactory.getLogger(CommentServiceImpl.class);

	@Autowired
	private CommentDao commentDao;
	@Autowired
	DynamicSQLQueryService dynamicSQLQuery;

	@Value("${data.dataset.download-path}")
	private String downloadPath;
	@Value("${data.dataset.prefix}")
	private String filePrefix;
	@Value("${data.dataset.ext}")
	private String fileExt;

	@Override
	public List<ViewCommentResponseDto> viewAllComments() {
		log.info("Listar comentarios");
		List<Comment> comments = commentDao.findAll();
		List<ViewCommentResponseDto> dtos = new ArrayList<>();

		for (Comment comment : comments) {
			ViewCommentResponseDto dto = new ViewCommentResponseDto();
			BeanUtils.copyProperties(comment, dto);
			dtos.add(dto);
		}

		return dtos;
	}

	@Override
	public CreateCommentResponseDto createComment(CreateCommentRequestDto request) {
		log.info("Crear comentario");
		Comment comment = new Comment();
		comment.setCommment(request.getComment());
		comment.setRead(false);
		comment.setRegDate(new Date());
		comment.setStatus(true);
		comment = commentDao.save(comment);

		return new CreateCommentResponseDto("Registro exitoso con ID: " + comment.getId().toString());
	}

	@Override
	public ReadCommentResponseDto readComment(ReadCommentsRequestDto request) throws FileNotFoundException {
		log.info("Buscar comentario");
		
		List<Long> readCommentsIds = new ArrayList<>();
		List<Long> notFoundCommentsIds = new ArrayList<>();
		
		for(Long id:request.getIds()) {
			Optional<Comment> comment = commentDao.findById(id);
			if (comment.isEmpty()) {
				notFoundCommentsIds.add(id);
			} else {
				comment.get().setRead(true);
				commentDao.save(comment.get());
				readCommentsIds.add(id);
			}
		}
		
		return new ReadCommentResponseDto(readCommentsIds, notFoundCommentsIds, "Ok");
	}

	@Override
	public DeleteCommentResponseDto deleteComment(DeleteCommentsRequestDto request) throws FileNotFoundException {
		log.info("Eliminar comentario");
		for (Long id : request.getIds()) {
			Optional<Comment> comment = commentDao.findById(id);

			if (comment.isEmpty())
				throw new FileNotFoundException("Comentario " + id.toString() + " no encontrado");
		}

		for (Long id : request.getIds()) {
			Optional<Comment> comment = commentDao.findById(id);
			if (comment.isPresent())
				commentDao.delete(comment.get());
		}

		return new DeleteCommentResponseDto("Comentarios eliminados con éxito");

	}

	@Override
	public CommentsFileResponseDto downloadCommentsFile(DownloadCommentsRequestDto request) {
		log.info("Descargar archivo de comentarios");
		CommentsFileResponseDto response = new CommentsFileResponseDto();
		String temporalDownloadPath = downloadPath.replace('/', File.separatorChar);

		if (!new File(temporalDownloadPath).exists()) {
			new File(temporalDownloadPath).mkdirs();
		}

		String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"));
		String fileName = filePrefix + "Generated_" + timeStamp;
		String newFilePath = downloadPath + fileName + fileExt;
		response.setName(newFilePath);

		if (!dynamicSQLQuery.downloadCommentsFile(request.getIds(), newFilePath))
			return response;

		InputStream targetFile = null;
		try {
			targetFile = new FileInputStream(new File(newFilePath));
			response.setFile(new InputStreamResource(targetFile));
		} catch (FileNotFoundException e) {
			log.error(e.getMessage());
		}

		return response;
	}
}
