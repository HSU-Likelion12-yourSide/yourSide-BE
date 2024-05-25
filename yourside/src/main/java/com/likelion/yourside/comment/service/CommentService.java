package com.likelion.yourside.comment.service;

import com.likelion.yourside.comment.dto.CommentCreateDto;
import com.likelion.yourside.util.response.CustomAPIResponse;
import org.springframework.http.ResponseEntity;

public interface CommentService {
    //댓글 데이터베이스에 추가하기
    ResponseEntity<CustomAPIResponse<?>> createComment(Long postingId, CommentCreateDto.Req req);

    //데이터베이스에 저장되어 있는 해당 게시글의 댓글 정보 얻어오기
    ResponseEntity<CustomAPIResponse<?>> getAllComment(Long postingId);

}