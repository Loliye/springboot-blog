package blog.controller.admin;

import blog.constant.WebConst;
import blog.controller.BaseController;
import blog.dto.LogAction;
import blog.dto.Types;
import blog.exception.TipException;
import blog.model.Bo.RestResponseBo;
import blog.model.Vo.UserVo;
import blog.service.ContentService;
import blog.service.LogService;
import blog.model.Vo.ContentVo;
import blog.model.Vo.ContentVoExample;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/admin/page")
public class PageController extends BaseController
{
    private static final Logger LOGGER = LoggerFactory.getLogger(PageController.class);

    @Resource
    private ContentService contentService;

    @Resource
    private LogService logService;

    //    首页
    @GetMapping("")
    public String index(HttpServletRequest request)
    {
        ContentVoExample contentVoExample = new ContentVoExample();
        contentVoExample.setOrderByClause("created desc");
        contentVoExample.createCriteria().andTypeEqualTo(Types.PAGE.getType());
        PageInfo<ContentVo> contentVoPageInfo = contentService.getArticlesWithpage(contentVoExample, 1, WebConst.MAX_POSTS);
        request.setAttribute("articles", contentVoPageInfo);
        return "admin/page_list";
    }

    //    新建文章
    @GetMapping("/new")
    public String newPage(HttpServletRequest request)
    {
        return "admin/page_edit";
    }

    //    查看具体
    @GetMapping("/{cid}")
    public String editPage(@PathVariable String cid, HttpServletRequest request)
    {
        ContentVo contentVo = contentService.getContents(cid);
        request.setAttribute("contents", contentVo);
        return "admin/page_edit";
    }

    //    发布新文章
    @PostMapping(value = "/publish")
    @ResponseBody
    @Transactional(rollbackFor = TipException.class)
    public RestResponseBo publishPage(@RequestParam String title, @RequestParam String content,
                                      @RequestParam String status, @RequestParam String slug,
                                      @RequestParam(required = false) Integer allowComment, @RequestParam(required = false) Integer allowPing, HttpServletRequest request)
    {

        UserVo users = this.user(request);
        ContentVo contents = new ContentVo();
        contents.setTitle(title);
        contents.setContent(content);
        contents.setStatus(status);
        contents.setSlug(slug);
        contents.setType(Types.PAGE.getType());
        if (null != allowComment)
            contents.setAllowComment(allowComment == 1);
        if (null != allowPing)
            contents.setAllowPing(allowPing == 1);
        contents.setAuthorId(users.getUid());

        try
        {
            contentService.publish(contents);
        } catch (Exception e)
        {
            String msg = "页面发布失败";
            if (e instanceof TipException)
            {
                msg = e.getMessage();
            } else
            {
                LOGGER.error(msg, e);
            }
            return RestResponseBo.fail(msg);
        }
        return RestResponseBo.ok();
    }

    //    修改文章
    @PostMapping(value = "/modify")
    @ResponseBody
    @Transactional(rollbackFor = TipException.class)
    public RestResponseBo modifyArticle(@RequestParam Integer cid, @RequestParam String title,
                                        @RequestParam String content,
                                        @RequestParam String status, @RequestParam String slug,
                                        @RequestParam(required = false) Integer allowComment, @RequestParam(required = false) Integer allowPing, HttpServletRequest request)
    {

        UserVo users = this.user(request);
        ContentVo contents = new ContentVo();
        contents.setCid(cid);
        contents.setTitle(title);
        contents.setContent(content);
        contents.setStatus(status);
        contents.setSlug(slug);
        contents.setType(Types.PAGE.getType());
        if (null != allowComment)
        {
            contents.setAllowComment(allowComment == 1);
        }
        if (null != allowPing)
        {
            contents.setAllowPing(allowPing == 1);
        }
        contents.setAuthorId(users.getUid());
        try
        {
            contentService.updateArticle(contents);
        } catch (Exception e)
        {
            String msg = "页面编辑失败";
            if (e instanceof TipException)
            {
                msg = e.getMessage();
            } else
            {
                LOGGER.error(msg, e);
            }
            return RestResponseBo.fail(msg);
        }
        return RestResponseBo.ok();
    }

    //    删除
    @RequestMapping(value = "/delete")
    @ResponseBody
    @Transactional(rollbackFor = TipException.class)
    public RestResponseBo delete(@RequestParam int cid, HttpServletRequest request)
    {
        try
        {
            contentService.deleteByCid(cid);
            logService.insertLog(LogAction.DEL_PAGE.getAction(), cid + "", request.getRemoteAddr(), this.getUid(request));
        } catch (Exception e)
        {
            String msg = "页面删除失败";
            if (e instanceof TipException)
            {
                msg = e.getMessage();
            } else
            {
                LOGGER.error(msg, e);
            }
            return RestResponseBo.fail(msg);
        }
        return RestResponseBo.ok();
    }


}
