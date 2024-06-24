package com.nc13.springBoard.controller;

import com.nc13.springBoard.model.BoardDTO;
import com.nc13.springBoard.model.ReplyDTO;
import com.nc13.springBoard.model.UserDTO;
import com.nc13.springBoard.service.BoardService;
import com.nc13.springBoard.service.ReplyService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/board/")
public class BoardController {
    @Autowired
    private BoardService boardService;
    @Autowired
    private ReplyService replyService;

    @GetMapping("showAll")
    public String moveToFirstPage() {
        return "redirect:/board/showAll/1";
    }

    //HttpSession - > 세션을 관리하기 위해 파라미터로 넣어줌
    // Model : Parameter로 선언만 해주면 bean 객체가 스프링이 알아서 만들어주고 관리해준다.
    // @PathVariable("변수명") {pageNo} - > @PathVariable int pageNo
    @GetMapping("showAll/{pageNo}")
    public String showAll(HttpSession session, Model model, @PathVariable int pageNo) {
        UserDTO logIn = (UserDTO) session.getAttribute("logIn");
        if(logIn == null) {
            return "redirect:/";
        }
        // 가장 마지막 페이지의 번호
        int maxPage = boardService.selectMaxPage();
        model.addAttribute("maxPage", maxPage);

        // 우리가 이제 pageNo를 사용하여
        // 시작 페이지 번호
        // 끝 페이지 번호
        // 을 계산해 주어야 한다
        // 이때에는 크게 3가지가 있다.
        // 1. 현재 페이지가 3이하일 경우
        // 시작: 1, 끝 5

        // 2. 현재 페이지가 최대 페이지 -2 이상일 경우
        // 시작: 최대 페이지 -4 끝: 최대 페이지
        // 3. 그외
        // 시작: 현재 페이지 -2 끝: 현재 페이지 + 2

        // 시작 페이지
        int startPage;

        // 끝 페이지
        int endPage;

        if (maxPage < 5) {
            startPage = 1;
            endPage = maxPage;
        } else if(pageNo <= 3) {
            startPage = 1;
            endPage = 5;
        } else if(pageNo >= maxPage - 2) {
            startPage = maxPage - 4;
            endPage = maxPage;
        } else {
            startPage = pageNo - 2;
            endPage = pageNo + 2;
        }

        model.addAttribute("curPage", pageNo);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        List<BoardDTO> list = boardService.selectAll(pageNo);
        model.addAttribute("list", list);

        return "board/showAll";
    }
    @GetMapping("write")
    public String showWriter(HttpSession session) {
        UserDTO logIn = (UserDTO) session.getAttribute("logIn");
        if(logIn == null) {
            return "redirect:/";
        }
        return "board/write";
    }


    @PostMapping("write")
    public String write(HttpSession session, BoardDTO boardDTO, MultipartFile[] file) {
        UserDTO logIn = (UserDTO) session.getAttribute("logIn");
        if(logIn == null) {
            return "redirect:/";
        }

        boardDTO.setWriterId(logIn.getId());

        String path ="c:\\uploads\\a\\bb\\cc";

        File pathDir = new File(path);
        if(!pathDir.exists()) {
            pathDir.mkdirs();
        }


        try {
            for(MultipartFile mf : file) {
                File f = new File(path, mf.getOriginalFilename());
                mf.transferTo(f);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        boardService.insert(boardDTO);

        return "redirect:/board/showOne/" + boardDTO.getId();
    }

    // 우리가 주소창에 있는 값을 매핑해줄 수 있다.
    @GetMapping("showOne/{id}")
    public String showOne(HttpSession session, @PathVariable int id, Model model, RedirectAttributes redirectAttributes) { // 아이디값을 받아오기 위해서 경로 안에 id값과 PathVariable 어노테이션 써줘야함
        UserDTO logIn = (UserDTO) session.getAttribute("logIn");
        if(logIn == null) {
            return "redirect:/";
        }

        BoardDTO boardDTO = boardService.selectOne(id);

        if(boardDTO == null) {
            redirectAttributes.addFlashAttribute("message", "해당 글 번호는 유효하지 않습니다.");
            return "redirect:/showMessage";
        }

        List<ReplyDTO> replyList = replyService.selectAll(id);

        model.addAttribute("boardDTO", boardDTO);
        model.addAttribute("replyList", replyList);


        return "board/showOne";
    }
    @GetMapping("update/{id}")
    public String showUpdate(@PathVariable int id, HttpSession session, RedirectAttributes redirectAttributes, Model model) {
        UserDTO logIn = (UserDTO) session.getAttribute("logIn");
        if(logIn == null) {
            return "redirect:/";
        }

        BoardDTO boardDTO = boardService.selectOne(id);

        if (boardDTO == null) {
            redirectAttributes.addFlashAttribute("message", "존재하지 않는 글 번호입니다.");
            return "redirect:/showMessage";
        }

        if(boardDTO.getWriterId() != logIn.getId()) {
            redirectAttributes.addFlashAttribute("message", "권한이 없습니다.");
            return "redirect:/showMessage";
        }




        model.addAttribute("boardDTO", boardDTO);

        return "board/update";
    }

    @PostMapping("update/{id}")
    public String update(@PathVariable int id, HttpSession session, RedirectAttributes redirectAttributes, BoardDTO attempt) {
        UserDTO logIn = (UserDTO) session.getAttribute("logIn");
        if (logIn == null) {
            return "redirect:/";
        }

        BoardDTO boardDTO = boardService.selectOne(id);
        if (boardDTO == null) {
            redirectAttributes.addFlashAttribute("message", "유효하지 않은 글 번호입니다.");
            return "redirect:/showMessage";
        }

        if(logIn.getId() != boardDTO.getWriterId()) {
            redirectAttributes.addFlashAttribute("message", "권한이 없습니다.");
            return "redirect:/showMessage";
        }

        attempt.setId(id);

        boardService.update(attempt);

        return "redirect:/board/showOne/" + id;
    }

    @GetMapping("delete/{id}")
    public String delete(@PathVariable int id, HttpSession session, RedirectAttributes redirectAttributes) {
        UserDTO logIn = (UserDTO) session.getAttribute("logIn");
        if(logIn == null) {
            return "redirect:/";
        }
        BoardDTO boardDTO = boardService.selectOne(id);
        if (boardDTO == null) {
            redirectAttributes.addFlashAttribute("message", "존재하지 않는 글번호");
            return "redirect:/showMessage";
        }

        if(boardDTO.getWriterId() != logIn.getId()) {
            redirectAttributes.addFlashAttribute("message", "권한 없음");
            return "redirect:/showMessage";
        }

        boardService.delete(id);
        return "redirect:/board/showAll";
    }

    // 일반 컨트롤러 안에
    // Restful API, JSON의 결곽밧을 리턴해야하는 경우
    // 맵핑 어노테이션 위에 ResponseBody 어노테이션을 붙여준다.


    // 주소로 반환하는 것이 아닌 Map의 결과값을 write.jsp에 리턴한다. (upload의 결과값을 전달)

    // MultipartHttpServletRequest Spring에서 파일을 업로드, 사진을 등록하기 위해서 작업 과정에 필요함
    // request값을 담고 있으면서, input의 file 또한 담아서 파일을 공유
    // getName : 파일의 이름을 구함
    // getOriginsFilename(): 업로드한 파일의 실제이름을 구함
    // transferTo(Filedest) : 업로드한 파일 데이터를 지정한 파일에 저장한다.
    @ResponseBody
    @PostMapping("uploads")
    public Map<String, Object> uploads(MultipartHttpServletRequest request) {
        Map<String, Object> resultMap = new HashMap<>();

        String uploadPath = "";

        MultipartFile file = request.getFile("upload");
        String fileName = file.getOriginalFilename();
        String extension = fileName.substring(fileName.lastIndexOf("."));
        String uploadName = UUID.randomUUID() + extension;

        // 톰켓에 돌아가는 주소값을 찾는 메소드? 방법
        String realPath = request.getServletContext().getRealPath("/board/uploads/");
        Path realDir = Paths.get(realPath);
        if(!Files.exists(realDir)) {
            try{
                Files.createDirectories(realDir);
            } catch(IOException e){
                e.printStackTrace();
            }
        }

        File uploadFile = new File(realPath + uploadName);
        try {
            file.transferTo(uploadFile);
        } catch (IOException e) {
            System.out.println("파일 전송 중 에러");
            e.printStackTrace();
        }

        uploadPath = "/board/uploads/" + uploadName;

        resultMap.put("uploaded", true);
        resultMap.put("url", uploadPath);
        return resultMap;
    }


}
