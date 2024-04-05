package kr.spring.care.board.model;

import java.util.Date;
import kr.spring.care.user.entity.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoardDTO {

	private Long num;
	private String title;
	private String writer;
	private String content;
	private Date regdate;
	private Long hitcount;
	private Long replycnt;
	
	private String regDateStr;
	
	//댓글
//	private List<Comment> comments;
	
	private User user;
	
	public BoardDTO(Board entity) {
    	this.num = entity.getNum();
    	this.title = entity.getTitle();
    	this.writer = entity.getWriter();
    	this.content = entity.getContent();
    	this.regdate = entity.getRegdate();
    	this.hitcount = entity.getHitcount();
    	this.replycnt = entity.getReplycnt();
    	this.user = entity.getUser();
    }
	
	

}
