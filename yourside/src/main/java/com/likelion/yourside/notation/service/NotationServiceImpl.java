package com.likelion.yourside.notation.service;

import com.likelion.yourside.domain.Notation;
import com.likelion.yourside.notation.dto.NotationDto;
import com.likelion.yourside.notation.dto.NotationListDto;
import com.likelion.yourside.notation.repository.NotationRepository;
import com.likelion.yourside.util.response.CustomAPIResponse;
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
public class NotationServiceImpl implements NotationService{

    private final NotationRepository notationRepository;

    //게시글 전체 조회
    @Override
    public ResponseEntity<CustomAPIResponse<?>> getAllNotation(){
        List<Notation> notations = notationRepository.findAll();

        //공지사항이 존재하지 않는 경우
        if(notations.isEmpty()){
            CustomAPIResponse<Void> res = CustomAPIResponse.createFailWithoutData(HttpStatus.NOT_FOUND.value(), "현재 등록된 공지사항이 없습니다.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
        }

        //변환
        List<NotationListDto.NotationResponse> notationResponses = new ArrayList<>();
        for (Notation notation : notations) {
            notationResponses.add(NotationListDto.NotationResponse.builder()
                            .id(notation.getId())
                            .title(notation.getTitle())
                            .isPinned(notation.isPinned())
                            .createdAt(notation.getCreatedAt())
                    .build());
        }

        //최종 데이터
        NotationListDto.SearchNotations searchNotations = new NotationListDto.SearchNotations(notationResponses);
        CustomAPIResponse<NotationListDto.SearchNotations> res = CustomAPIResponse.createSuccess(HttpStatus.OK.value(), searchNotations, "공지사항 조회 성공");
        return ResponseEntity.status(HttpStatus.OK).body(res);

    }


    //게시글 하나 조회
    @Override
    public ResponseEntity<CustomAPIResponse<?>> getOneNotation(Long notationId){
        Optional<Notation> optionalNotation = notationRepository.findById(notationId);
        //notationId에 해당하는 공지사항이 없을 경우 Optional<NULL>을 가짐

        //해당 공지사항이 존재하지 않을 경우
        if (optionalNotation.isEmpty()) {
            CustomAPIResponse<Void> res = CustomAPIResponse.createFailWithoutData(HttpStatus.NOT_FOUND.value(), "해당하는 공지사항을 찾을 수 없습니다.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
        }

        //정상적으로 조회가 된 경우
        Notation notation = optionalNotation.get();
        NotationDto.NotationResponse notationResponse = new NotationDto.NotationResponse(
                notation.getTitle(),
                notation.getContent(),
                notation.getCreatedAt(),
                notation.isPinned()
        );

        CustomAPIResponse<NotationDto.NotationResponse> res = CustomAPIResponse.createSuccess(HttpStatus.OK.value(), notationResponse, "공지사항 조회 성공");
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }
}