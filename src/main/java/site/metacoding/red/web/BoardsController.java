package site.metacoding.red.web;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import lombok.RequiredArgsConstructor;
import site.metacoding.red.domain.boards.Boards;
import site.metacoding.red.domain.boards.BoardsDao;
import site.metacoding.red.domain.users.Users;
import site.metacoding.red.web.dto.request.boards.WriteDto;
import site.metacoding.red.web.dto.response.boards.MainDto;
import site.metacoding.red.web.dto.response.boards.PagingDto;

@RequiredArgsConstructor
@Controller
public class BoardsController {

	private final HttpSession session;
	private final BoardsDao boardsDao;
	// @PostMapping("/boards/{id}/delete")
	// @PostMapping("/boards/{id}/update")
	
	
	//1. 만약 50번을 id로 주고 삭제 한다고 치자.
	// -> 50번이 없어도 0행이 삭제 되었다고 실행이 된다. 
	// 이것을 체크 해주어야 한다. 
	// 트랜젝션 관리를 위해서 영속화를 한번 해주는것이 좋다 !
	@PostMapping("/boards/{id}/delete")
	public String deleteBoards(@PathVariable Integer id) {
		
		Users principal =  (Users)session.getAttribute("principal");
		//1. 영속화 (트랜젝션 관리를 위해 해주는것이 좋다)
		Boards boardsPs = boardsDao.findById(id);
		// 로그인 인증 체크
		if(principal == null) {
			System.out.println("로그인 해주세요.");
			return "redirect:/";
		}
		// 권한 체크 (principal.getId가 boardsPs의 users아이디가 동일 해야한다. 
		
		if(principal.getId() != boardsPs.getUsersId()) {
			System.out.println("삭제 권한이 없습니다.");
			return "redirect:/";
		}

		if(boardsPs == null) {// if는 비정상 로직을 타게해서 걸러내는 필터 역할로 써주는 것이 좋다 . 
			return "redirect:/boards/" + id;
		}
		// null값 체크 했으므로 아래 코드에서 null이 들어갈 일은 없다. 
		boardsDao.delete(id);
		return "redirect:/";
	}
	
	@PostMapping("/boards")
	public String writeBoards(WriteDto writeDto) {
		// 1번 세션에 접근해서 세션값을 확인한다. 그때 Users로 다운캐스팅하고 키값은 principal로 한다.
		Users principal = (Users) session.getAttribute("principal");
		
		// 2번 pricipal null인지 확인하고 null이면 loginForm 리다이렉션해준다.
		if(principal == null) {
			return "redirect:/loginForm";
		}
		// 3번 BoardsDao에 접근해서 insert 메서드를 호출한다.
		// 조건 : dto를 entity로 변환해서 인수로 담아준다.
		// 조건 : entity에는 세션의 principal에 getId가 필요하다.
		boardsDao.insert(writeDto.toEntity(principal.getId()));
		
		return "redirect:/";
	}
	
	
	//http://localhost:8000/ -> 쿼리스트링이 비어있으므로 null  , 디폴트 값을 만들어주어야한다.
	// http://localhost:8000/?page=0 
	@GetMapping({"/", "/boards"})
	public String getBoardList(Model model, Integer page) {// pk가 아니면 모두 쿼리 스트링으로 받는다.(page), 0-> 0 , 1-> 10, 2 -> 20
		if(page == null) page =0;
		int startNum = page * 3;
		
		PagingDto paging = boardsDao.paging(page);
		List<MainDto> boardsList = boardsDao.findAll(startNum);
		
		//paging.set 머시기로 dto 완성 
		final int blockCount = 5;

		int currentBlock = page / blockCount;
		int startPageNum = 1 + blockCount * currentBlock;
		int lastPageNum = 5 + blockCount * currentBlock;

		if (paging.getTotalPage() < lastPageNum) {
			lastPageNum = paging.getTotalPage();
		}

		paging.setBlockCount(blockCount);
		paging.setCurrentBlock(currentBlock);
		paging.setStartPageNum(startPageNum);
		paging.setLastPageNum(lastPageNum);
		

		model.addAttribute("boardsList", boardsList);
		model.addAttribute("paging", paging);
		return "boards/main";
	}
	
	@GetMapping("/boards/{id}")
	public String getBoardList(@PathVariable Integer id, Model model) {
		model.addAttribute("boards", boardsDao.findById(id));
		return "boards/detail";
	}
	
	@GetMapping("/boards/writeForm")
	public String writeForm() {
		Users principal = (Users) session.getAttribute("principal");
		if(principal == null) {
			return "redirect:/loginForm";
		}
		
		return "boards/writeForm";
	}
}
