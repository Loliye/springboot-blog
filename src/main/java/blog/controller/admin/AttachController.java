package blog.controller.admin;

import blog.constant.WebConst;
import blog.controller.BaseController;
import blog.dto.LogAction;
import blog.dto.Types;
import blog.exception.TipException;
import blog.model.Bo.RestResponseBo;
import blog.service.AttachService;
import blog.model.Vo.AttachVo;
import blog.model.Vo.UserVo;
import blog.service.LogService;
import blog.utils.Commons;
import blog.utils.TaleUtils;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin/attach")
public class AttachController extends BaseController
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AttachController.class);

    public static final String CLASSPATH = TaleUtils.getUplodFilePath();

    @Resource
    private AttachService attachService;

    @Resource
    private LogService logService;

    /**
     * 附件页面
     *
     * @param request
     * @param page
     * @param limit
     * @return
     */
    @GetMapping("")
    public String index(HttpServletRequest request, @RequestParam(value = "page", defaultValue = "1") int page, @RequestParam(value = "limit", defaultValue = "12") int limit)
    {
        PageInfo<AttachVo> attachVoPageInfo = attachService.getAttachs(page, limit);
        request.setAttribute("attachs", attachVoPageInfo);
        request.setAttribute(Types.ATTACH_URL.getType(), Commons.site_option(Types.ATTACH_URL.getType(), Commons.site_url()));

        request.setAttribute("max_file_size", WebConst.MAX_FILE_SIZE / 1024);
        return "admin/attach";
    }

    @PostMapping("/upload")
    @ResponseBody
    @Transactional(rollbackFor = TipException.class)
    public RestResponseBo upload(HttpServletRequest request, @RequestParam("file") MultipartFile[] multipartFiles)
    {
        UserVo users = this.user(request);
        Integer uid = users.getUid();
        List<String> errorsFile = new ArrayList<>();
        try
        {
            for (MultipartFile multipartFile : multipartFiles)
            {
                String fName = multipartFile.getOriginalFilename();
                if (multipartFile.getSize() <= WebConst.MAX_FILE_SIZE)
                {
                    String fKey = TaleUtils.getFileKey(fName);
                    String fType = TaleUtils.isImage(multipartFile.getInputStream()) ? Types.IMAGE.getType() : Types.FILE.getType();
                    File file = new File(CLASSPATH + fKey);
                    FileCopyUtils.copy(multipartFile.getInputStream(), new FileOutputStream(file));
                    attachService.save(fName, fKey, fType, uid);
                } else
                {
                    errorsFile.add(fName);
                }
            }
        } catch (Exception e)
        {
            return RestResponseBo.fail();
        }
        return RestResponseBo.ok(errorsFile);
    }


    @RequestMapping("/delete")
    @ResponseBody
    @Transactional(rollbackFor = TipException.class)
    public RestResponseBo delete(@RequestParam Integer id, HttpServletRequest request)
    {
        try
        {
            AttachVo attachVo = attachService.selectById(id);
            if (attachVo == null)
                return RestResponseBo.fail("不存在该附件！");
            attachService.deleteById(id);
            new File(CLASSPATH + attachVo.getFkey()).delete();
            logService.insertLog(LogAction.DEL_ARTICLE.getAction(), attachVo.getFkey(), request.getRemoteUser(), this.getUid(request));

        } catch (Exception e)
        {
            String msg = "附件删除失败！";
            if (e instanceof TipException)
                msg = e.getMessage();
            else LOGGER.error(msg, e);
            return RestResponseBo.fail(msg);
        }
        return RestResponseBo.ok();
    }


}
