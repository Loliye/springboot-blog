package blog.controller.admin;

import blog.controller.BaseController;
import blog.exception.TipException;
import blog.model.Bo.RestResponseBo;
import blog.model.Vo.CommentVo;
import blog.model.Vo.CommentVoExample;
import blog.model.Vo.UserVo;
import blog.service.CommentService;
import blog.utils.TaleUtils;
import com.github.pagehelper.PageInfo;
import com.vdurmont.emoji.EmojiParser;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/admin/comments")
public class CommentController extends BaseController
{
    private static final Logger LOGGER = LoggerFactory.getLogger(CommentController.class);


    @Resource
    private CommentService commentService;

    //评论列表
    @GetMapping("")
    public String index(@RequestParam(value = "page", defaultValue = "1") int page,
                        @RequestParam(value = "limit", defaultValue = "15") int limit,
                        HttpServletRequest request)
    {
        UserVo users = this.user(request);
        CommentVoExample commentVoExample = new CommentVoExample();
        commentVoExample.setOrderByClause("coid desc");
        commentVoExample.createCriteria().andAuthorIdNotEqualTo(users.getUid());
        PageInfo<CommentVo> comments = commentService.getCommentWithPage(commentVoExample, page, limit);
        request.setAttribute("comments", comments);
        return "admin/comment_list";
    }

    //删除评论
    @PostMapping("/delete")
    @ResponseBody
    @Transactional(rollbackFor = TipException.class)
    public RestResponseBo delete(@RequestParam Integer coid)
    {
        try
        {
            CommentVo commentVo = commentService.getCommentById(coid);
            if (commentVo == null)
                return RestResponseBo.fail("不存在该条评论");
            commentService.delete(coid, commentVo.getCid());

        } catch (Exception e)
        {
            String msg = "评论删除失败";
            if (e instanceof TipException)
                msg = e.getMessage();
            else LOGGER.error(msg, e);
            return RestResponseBo.fail(msg);
        }
        return RestResponseBo.ok();
    }

    //更新状态
    @PostMapping("/status")
    @ResponseBody
    @Transactional(rollbackFor = TipException.class)
    public RestResponseBo status(@RequestParam Integer coid, @RequestParam String status)
    {
        try
        {
            CommentVo comment = new CommentVo();
            comment.setCoid(coid);
            comment.setStatus(status);
            commentService.update(comment);
        } catch (Exception e)
        {
            String msg = "操作失败";
            if (e instanceof TipException)
                msg = e.getMessage();
            else LOGGER.error(msg, e);
            return RestResponseBo.fail(msg);
        }
        return RestResponseBo.ok();
    }

    @PostMapping("")
    @ResponseBody
    @Transactional(rollbackFor = TipException.class)
    public RestResponseBo reply(@RequestParam Integer coid, @RequestParam String content,
                                HttpServletRequest request)
    {
        if (coid == null || StringUtils.isBlank(content))
            return RestResponseBo.fail("请输入完整评论信息");

        if (content.length() > 2000)
            return RestResponseBo.fail("请输入2000字以内的回复");
        CommentVo comment = commentService.getCommentById(coid);
        if (comment == null)
            return RestResponseBo.fail("不存在该条评论");
        UserVo user = this.user(request);
        content = TaleUtils.cleanXSS(content);
        content = EmojiParser.parseToAliases(content);
        CommentVo comments = new CommentVo();
        comments.setAuthor(user.getUsername());
        comments.setAuthorId(user.getUid());
        comments.setCid(comment.getCid());
        comments.setIp(request.getRemoteAddr());
        comments.setUrl(user.getHomeUrl());
        comments.setContent(content);
        comments.setMail(user.getEmail());
        comments.setParent(coid);

        try
        {
            commentService.insertComment(comments);
            return RestResponseBo.ok();
        } catch (Exception e)
        {
            String msg = "回复失败";
            if (e instanceof TipException)
            {
                msg = e.getMessage();
            } else
            {
                LOGGER.error(msg, e);
            }
            return RestResponseBo.fail(msg);
        }
    }

}
