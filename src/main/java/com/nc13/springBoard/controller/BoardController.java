package com.nc13.springBoard.controller;

import com.nc13.springBoard.model.BoardDTO;
import com.nc13.springBoard.model.UserDTO;
import com.nc13.springBoard.service.BoardService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/board/")
public class BoardController {
    @Autowired
    private BoardService boardService;

    @GetMapping("showAll")
    public String showAll(HttpSession session, Model model) {
        UserDTO logIn = (UserDTO) session.getAttribute("logIn");
        if(logIn == null) {
            return "redirect:/";
        }

        List<BoardDTO> list = boardService.selectAll();
        model.addAttribute("list", list);

        return "board/showAll";
    }
}
