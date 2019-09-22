package blog.controller.admin;

import blog.controller.BaseController;
import blog.dto.LogAction;
import blog.dto.Types;
import blog.exception.TipException;
import blog.model.Bo.RestResponseBo;
import blog.model.Vo.ContentVo;
import blog.model.Vo.ContentVoExample;
import blog.model.Vo.MetaVo;
import blog.model.Vo.UserVo;
import blog.service.ContentService;
import blog.service.LogService;
import blog.service.MetaService;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping("/admin/article")
@Transactional(rollbackFor = TipException.class)
public class ArticleController extends BaseController
{

    private static final Logger LOGGER = LoggerFactory.getLogger(ArticleController.class);
    @Resource
    private ContentService contentService;
    @Resource
    private MetaService metaService;
    @Resource
    private LogService logService;

    //文章列表
    @GetMapping(value = "")
    public String index(@RequestParam(value = "page", defaultValue = "1") int page,
                        @RequestParam(value = "limit", defaultValue = "15") int limit, HttpServletRequest request)
    {
        ContentVoExample contentVoExample = new ContentVoExample();
        contentVoExample.setOrderByClause("created desc");
        contentVoExample.createCriteria().andTypeEqualTo(Types.ARTICLE.getType());
        PageInfo<ContentVo> contentVoPageInfo = contentService.getArticlesWithpage(contentVoExample, page, limit);
        request.setAttribute("articles", contentVoPageInfo);
        return "admin/article_list";
    }

    //    文章发布
    @GetMapping("/publish")
    public String newArticle(HttpServletRequest request)
    {
        List<MetaVo> categories = metaService.getMetas(Types.CATEGORY.getType());
        request.setAttribute("categories", categories);
        return "admin/article_edit";
    }

    //    文章编辑
    @GetMapping("/{id}")
    public String editArticle(@PathVariable String id, HttpServletRequest request)
    {
        ContentVo contentVo = contentService.getContents(id);
        request.setAttribute("contents", contentVo);
        List<MetaVo> categories = metaService.getMetas(Types.CATEGORY.getType());
        request.setAttribute("categories", categories);
        request.setAttribute("active", "article");
        return "admin/article_edit";
    }

    //    文章发布
    @PostMapping("/publish")
    @ResponseBody
    @Transactional(rollbackFor = TipException.class)
    public RestResponseBo publishArticle(ContentVo contentVo, HttpServletRequest request)
    {
        UserVo userVo = this.user(request);
        contentVo.setAuthorId(userVo.getUid());
        contentVo.setType(Types.ARTICLE.getType());
        if (StringUtils.isBlank(contentVo.getCategories()))
            contentVo.setCategories("默认分类");
        try
        {
            contentService.publish(contentVo);
        } catch (Exception e)
        {
            String msg = "文章发布失败";
            if (e instanceof Exception)
                msg = e.getMessage();
            else LOGGER.error(msg, e);
            return RestResponseBo.fail(msg);
        }
        return RestResponseBo.ok();
    }

    //    修改文章
    @PostMapping("/modify")
    @ResponseBody
    @Transactional(rollbackFor = TipException.class)
    public RestResponseBo modify(ContentVo contentVo, HttpServletRequest request)
    {
        UserVo users = this.user(request);
        contentVo.setAuthorId(users.getUid());
        contentVo.setType(Types.ARTICLE.getType());
        try
        {
            contentService.updateArticle(contentVo);
        } catch (Exception e)
        {
            String msg = "文章编辑失败";
            if (e instanceof Exception)
                msg = e.getMessage();
            else LOGGER.error(msg, e);
            return RestResponseBo.fail(msg);
        }
        return RestResponseBo.ok();
    }

    //    删除文章
    @RequestMapping(value = "/delete")
    @ResponseBody
    @Transactional(rollbackFor = TipException.class)
    public RestResponseBo delete(@RequestParam int cid, HttpServletRequest request)
    {
        try
        {
            contentService.deleteByCid(cid);
            logService.insertLog(LogAction.DEL_ARTICLE.getAction(), cid + "", request.getRemoteAddr(), this.getUid(request));
        } catch (Exception e)
        {
            String msg = "文章删除失败";
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
