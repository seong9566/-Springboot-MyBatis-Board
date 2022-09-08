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
import site.metacoding.red.web.dto.request.boards.UpdateDto;
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
	
	// 업데이트가 완료 되었을 경우 ->수정한 해당 글의 위치로 이동.
	@PostMapping("/boards/{id}/update")
	public String update(@PathVariable Integer id,UpdateDto updateDto) {
		Users principal =  (Users)session.getAttribute("principal");
		//1 . 영속화
		Boards boardsPs = boardsDao.findById(id);
		//없는 번호 요청
		if(boardsPs == null) {
			return "errors/badPage";
		}
		//인증 체크 
		if(principal == null) {
			System.out.println("로그인 해주세요.");
			return "boards/loginForm";
		}
		
		// 권한 체크 (principal.getId가 boardsPs의 users아이디가 동일 해야한다. 
		if(principal.getId() != boardsPs.getUsersId()) {
			System.out.println("권한이 없습니다.");
			return "errors/badPage";
		}
		//2. 변경
		boardsPs.글수정(updateDto);
		//3. 수정
		boardsDao.update(boardsPs);
		return "redirect:/boards/"+id;
	}
	
	@GetMapping("/boards/{id}/updateForm")// 해당 id를 selete를 가져와 업데이트 할 수 있는 form을 줄게.
	public String updateForm(@PathVariable Integer id,Model model) {
		// 업데이트도 권한, 인증, 모두 체크 해주어야 한다.
		Users principal =  (Users)session.getAttribute("principal");
		Boards boardsPs = boardsDao.findById(id);
		//없는 번호 요청
		if(boardsPs == null) {
			return "errors/badPage";
		}
		//인증 체크 
		if(principal == null) {
			System.out.println("로그인 해주세요.");
			return "boards/loginForm";
		}
		
		// 권한 체크 (principal.getId가 boardsPs의 users아이디가 동일 해야한다. 
		if(principal.getId() != boardsPs.getUsersId()) {
			System.out.println("권한이 없습니다.");
			return "errors/badPage";
		}
		
		model.addAttribute("boards", boardsPs);
		return "boards/updateForm";
	}
	
	
	//1. 만약 50번을 id로 주고 삭제 한다고 치자.
	// -> 50번이 없어도 0행이 삭제 되었다고 실행이 된다. 
	// 이것을 체크 해주어야 한다. 
	// 트랜젝션 관리를 위해서 영속화를 한번 해주는것이 좋다 !
	@PostMapping("/boards/{id}/delete")
	public String deleteBoards(@PathVariable Integer id) {
		
		Users principal =  (Users)session.getAttribute("principal");
		//1. 영속화 (트랜젝션 관리를 위해 해주는것이 좋다)
		Boards boardsPs = boardsDao.findById(id);
		
		
//=================공통 로직==================================
		// 로그인 인증 체크
		// 비정상 요청 체크
		if(boardsPs == null) {// if는 비정상 로직을 타게해서 걸러내는 필터 역할로 써주는 것이 좋다 . 
			return "redirect:/boards/" + id;
		}
		//인증 체크 
		if(principal == null) {
			System.out.println("로그인 해주세요.");
			return "redirect:/loginForm";
		}
		
		// 권한 체크 (principal.getId가 boardsPs의 users아이디가 동일 해야한다. 
		if(principal.getId() != boardsPs.getUsersId()) {
			System.out.println("삭제 권한이 없습니다.");
			return "redirect:/boards/"+id;
		}
//=================공통 로직==================================
		// null값 체크 했으므로 아래 코드에서 null이 들어갈 일은 없다. 
		boardsDao.delete(id); // 핵심 로직 
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
	@GetMapping({ "/", "/boards" })
	public String getBoardList(Model model, Integer page) { // 0 -> 0, 1->10, 2->20
		if (page == null)
			page = 0;
		int startNum = page * 3;

		List<MainDto> boardsList = boardsDao.findAll(startNum);
		PagingDto paging = boardsDao.paging(page);
		paging.makeBlockInfo();
		
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
