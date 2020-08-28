package com.mex.whytter.dto;

import com.fasterxml.jackson.annotation.JsonView;
import com.mex.whytter.domain.Message;
import com.mex.whytter.domain.Views;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@JsonView(Views.FullMessage.class)
public class MessagePageDto {
    private List<Message> messages;
    private int currentPage;
    private int totalPages;

}
