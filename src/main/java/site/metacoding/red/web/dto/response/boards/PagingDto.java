package site.metacoding.red.web.dto.response.boards;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PagingDto {
   private Integer totalCount;
   private Integer totalPage;
   private Integer currentPage;
   private boolean isLast;
   private boolean isFirst;
   
   private Integer blockPage; // 변수
   private Integer blockCount; // 한페이지에 페이지 넘버 개수(5)1-5,6-10 -> 상수 
   private Integer startPageNum; // 1 ->6 -> 11  변수 
   private Integer lastPageNum; // 5 -> 10 -> 15  변수 
 
}
// getter가 만들어지면 isLast() 이름으로 만들어짐
//el에서는 last,first 로 찾음