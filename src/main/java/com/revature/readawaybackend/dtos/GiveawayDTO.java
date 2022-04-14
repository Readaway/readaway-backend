package com.revature.readawaybackend.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
public class GiveawayDTO {
    int id;
    Timestamp startTime;
    Timestamp endTime;
    String isbn;
    UserDTO creator;
    UserDTO winner;
    List<CommentDTO> comments;
    Set<UserDTO> entrants;
}
