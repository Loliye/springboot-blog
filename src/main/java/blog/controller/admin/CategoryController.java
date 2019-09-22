package blog.controller.admin;

import blog.constant.WebConst;
import blog.controller.BaseController;
import blog.dto.MetaDto;
import blog.dto.Types;
import blog.exception.TipException;
import blog.model.Bo.RestResponseBo;
import blog.service.MetaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping("/admin/category")
public class CategoryController extends BaseController
{
    private static final Logger LOGGER = LoggerFactory.getLogger(CategoryController.class);

    @Resource
    private MetaService metaService;

    //    分类页
    @GetMapping("")
    public String index(HttpServletRequest request)
    {
        List<MetaDto> categories = metaService.getMetaList(Types.CATEGORY.getType(), null, WebConst.MAX_POSTS);
        List<MetaDto> tags = metaService.getMetaList(Types.TAG.getType(), null, WebConst.MAX_POSTS);
        request.setAttribute("categories", categories);
        request.setAttribute("tags", tags);
        return "admin/category";
    }

    @PostMapping(value = "/save")
    @ResponseBody
    @Transactional(rollbackFor = TipException.class)
    public RestResponseBo saveCategory(@RequestParam String cname, @RequestParam Integer mid)
    {
        try
        {
            metaService.saveMeta(Types.CATEGORY.getType(), cname, mid);
        } catch (Exception e)
        {
            String msg = "分类保存失败";
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

    /**
     * 删除分类
     *
     * @param mid
     * @return
     */
    @RequestMapping(value = "/delete")
    @ResponseBody
    @Transactional(rollbackFor = TipException.class)
    public RestResponseBo delete(@RequestParam int mid)
    {
        try
        {
            metaService.delete(mid);
        } catch (Exception e)
        {
            String msg = "删除失败";
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
