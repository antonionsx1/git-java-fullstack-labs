package org.iadb.lmw.service;

import java.io.FileNotFoundException;
import java.util.List;

import org.iadb.lmw.dto.CommentsFileResponseDto;
import org.iadb.lmw.dto.CreateCommentRequestDto;
import org.iadb.lmw.dto.CreateCommentResponseDto;
import org.iadb.lmw.dto.DeleteCommentResponseDto;
import org.iadb.lmw.dto.DeleteCommentsRequestDto;
import org.iadb.lmw.dto.DownloadCommentsRequestDto;
import org.iadb.lmw.dto.ReadCommentResponseDto;
import org.iadb.lmw.dto.ReadCommentsRequestDto;
import org.iadb.lmw.dto.ViewCommentResponseDto;

public interface CommentService {

    List<ViewCommentResponseDto> viewAllComments();

    CreateCommentResponseDto createComment(CreateCommentRequestDto request);

    DeleteCommentResponseDto deleteComment(DeleteCommentsRequestDto request) throws FileNotFoundException;

    CommentsFileResponseDto downloadCommentsFile(DownloadCommentsRequestDto request);

    ReadCommentResponseDto readComment(ReadCommentsRequestDto request) throws FileNotFoundException;

}
