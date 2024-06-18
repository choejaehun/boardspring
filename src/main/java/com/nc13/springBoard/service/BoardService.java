package com.nc13.springBoard.service;


import com.nc13.springBoard.model.BoardDTO;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final String NAMESPACE = "com.nc13.mappers.BoardMapper";

    @Autowired
    private SqlSession session;

    public List<BoardDTO> selectAll() {
        return session.selectList(NAMESPACE + ".selectAll");
    }

}
