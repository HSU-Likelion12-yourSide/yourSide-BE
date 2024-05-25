package com.likelion.yourside.comment.service;
import com.likelion.yourside.comment.dto.CommentCreateDto;
import com.likelion.yourside.comment.dto.CommentLikeDto;
import com.likelion.yourside.comment.dto.CommentListDto;
import com.likelion.yourside.comment.repository.CommentRepository;
import com.likelion.yourside.domain.Comment;
import com.likelion.yourside.domain.Likes;
import com.likelion.yourside.domain.Posting;
import com.likelion.yourside.domain.User;
import com.likelion.yourside.likes.repository.LikesRepository;
import com.likelion.yourside.posting.repository.PostingRepository;
import com.likelion.yourside.user.repository.UserRepository;
import com.likelion.yourside.util.response.CustomAPIResponse;
import jakarta.validation.Valid;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Builder
public class CommentServiceImpl implements CommentService{
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostingRepository postingRepository;
    private final LikesRepository likesRepository;

    //댓글 작성 -----------------------------------------------------------------------------------------------
    @Override
    public ResponseEntity<CustomAPIResponse<?>> createComment(CommentCreateDto.Req req) {
        User user = userRepository.findById(req.getUser_id()).orElseThrow();

        Optional<Posting> optionalPosting = postingRepository.findById(req.getPosting_id());
        //해당 게시글이 없는 경우 : 404
        if (optionalPosting.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(CustomAPIResponse.createFailWithoutData(HttpStatus.NOT_FOUND.value(), "게시글이 삭제되었거나 존재하지 않는 게시글입니다."));
        }
        Posting posting = optionalPosting.get();

        //Comment 엔티티 객체 생성 후 저장
        Comment comment = req.toEntity(posting,req, user);
        Comment savedComment = commentRepository.save(comment);

        //댓글 작성 성공 : 201
        CustomAPIResponse<?> res = CustomAPIResponse.createSuccessWithoutData(HttpStatus.CREATED.value(),  "댓글이 작성되었습니다.");
        return ResponseEntity.ok(res);
    }


    //댓글 전체 조회 -------------------------------------------------------------------------------------------
    @Override
    public ResponseEntity<CustomAPIResponse<?>> getAllComment(Long postingId) {
        Optional<Posting> optionalPosting = postingRepository.findById(postingId);
        Posting posting = optionalPosting.get();
        List<Comment> comments = commentRepository.findAllbyPosting(posting);

        //댓글 전체 조회 성공(댓글 존재하지 않음) : 200
        if (comments.isEmpty()) {
            CustomAPIResponse<?> res = CustomAPIResponse.createFailWithoutData(HttpStatus.OK.value(), "작성한 댓글이 없습니다.");
            return ResponseEntity.ok(res);
        }

        //해당 게시글이 없는 경우 : 404
        if (optionalPosting.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(CustomAPIResponse.createFailWithoutData(HttpStatus.NOT_FOUND.value(), "게시글이 존재하지 않습니다."));
        }

        List<CommentListDto.CommentResponse> commentResponses = new ArrayList<>();
        //List<Comment> -> List<CommentListDto.CommentResponse>작업
        for (Comment comment : comments) {
            Long userId = comment.getUser().getId();
            Long commentId = comment.getId();
            boolean isLiked = likesRepository.existsByUserIdAndCommentId(userId, commentId);

            commentResponses.add(CommentListDto.CommentResponse.builder()
                    .nickname(comment.getUser().getNickname())
                    .createdAt(comment.getCreatedAt().toLocalDate())
                    .content(comment.getContent())
                    .isLiked(isLiked)
                    .likes(comment.getLikes())
                    .build());
        }

        //사용자에게 반환하기 위한 최종 데이터
        CommentListDto.SearchCommentRes searchCommentRes = new CommentListDto.SearchCommentRes(commentResponses);
        CustomAPIResponse<CommentListDto.SearchCommentRes> res = CustomAPIResponse.createSuccess(HttpStatus.OK.value(), searchCommentRes, "댓글 조회가 완료되었습니다.");

        //댓글 전체 조회 성공(댓글 존재) : 200
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(res);
    }

    //좋아요 추가--------------------------------------------------------------------------------------
    @Override
    public ResponseEntity<CustomAPIResponse<?>> addLikeToComment(CommentLikeDto.Req req) {
        // DTO 검증 : API에는 없음(프론트 측에서 해서 줄 것. 그러나 이미 만들었으니 통신할 때 편하라고 남겨둠)
        if (req.getUserId() == null || req.getCommentId() == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(CustomAPIResponse.createFailWithoutData(HttpStatus.BAD_REQUEST.value(), "user_id와 comment_id는 필수 값입니다."));
        }

        //DB에 해당 댓글이 없는 경우 : 404
        Optional<Comment> optionalComment = commentRepository.findById(req.getCommentId());
        if (optionalComment.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(CustomAPIResponse.createFailWithoutData(HttpStatus.NOT_FOUND.value(), "해당하는 댓글이 존재하지 않습니다."));
        }

        User user = userRepository.findById(req.getUserId()).orElseThrow();
        Comment comment = optionalComment.get();

        //likes 스키마에 user_id, comment_id를 가지는 레코드 추가
        Likes likes = req.toEntity(user, comment);
        likesRepository.save(likes);

        //Comment 스키마에 likes_count +1
        int likesCount = comment.getLikes() + 1;
        comment.changeLikes(likesCount);
        commentRepository.save(comment); // 변경 사항 저장

        //좋아요 추가 성공 : 201
        CustomAPIResponse<?> res = CustomAPIResponse.createSuccessWithoutData(HttpStatus.CREATED.value(), "해당 댓글을 좋아요 하셨습니다.");
        return ResponseEntity.ok(res);
    }

    //좋아요 취소--------------------------------------------------------------------------------------
    @Override
    public ResponseEntity<CustomAPIResponse<?>> removeLikeFromComment(CommentLikeDto.Req req) {

        // DTO 검증 : API에는 없음(프론트 측에서 해서 줄 것. 그러나 이미 만들었으니 통신할 때 편하라고 남겨둠)
        if (req.getUserId() == null || req.getCommentId() == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(CustomAPIResponse.createFailWithoutData(HttpStatus.BAD_REQUEST.value(), "user_id와 comment_id는 필수 값입니다."));
        }

        //DB에 해당 댓글이 없는 경우 : 404
        Optional<Comment> optionalComment = commentRepository.findById(req.getCommentId());
        if (optionalComment.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(CustomAPIResponse.createFailWithoutData(HttpStatus.NOT_FOUND.value(), "해당하는 댓글이 존재하지 않습니다."));
        }

        User user = userRepository.findById(req.getUserId()).orElseThrow();
        Comment comment = optionalComment.get();

        //likes 스키마에서 user_id, comment_id를 가지는 레코드 삭제
        Optional<Likes> optionalLikes = likesRepository.findByUserAndComment(user, comment);//user, comment 필드를 가지는 likes 찾기

        if (optionalLikes.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(CustomAPIResponse.createFailWithoutData(HttpStatus.NOT_FOUND.value(), "좋아요를 하신 적이 없습니다."));
        }

        Likes likes = optionalLikes.get();
        likesRepository.delete(likes);

        //Comment 스키마에 likes_count +1
        int likesCount = comment.getLikes() - 1;
        comment.changeLikes(likesCount);
        commentRepository.save(comment); // 변경 사항 저장

        //좋아요 삭제 성공 : 201
        CustomAPIResponse<?> res = CustomAPIResponse.createSuccessWithoutData(HttpStatus.CREATED.value(), "해당 댓글에 좋아요를 취소하셨습니다.");
        return ResponseEntity.ok(res);
    }
}
