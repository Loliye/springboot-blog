package blog.service.impl;

import blog.constant.WebConst;
import blog.controller.admin.AttachController;
import blog.dao.AttachVoMapper;
import blog.dao.CommentVoMapper;
import blog.dao.ContentVoMapper;
import blog.dao.MetaVoMapper;
import blog.dto.MetaDto;
import blog.dto.Types;
import blog.exception.TipException;
import blog.model.Bo.StatisticsBo;
import blog.model.Vo.*;
import blog.utils.ZipUtils;
import blog.model.Bo.ArchiveBo;
import blog.model.Bo.BackResponseBo;
import blog.service.SiteService;
import blog.utils.DateKit;
import blog.utils.TaleUtils;
import blog.utils.backup.Backup;
import com.github.pagehelper.PageHelper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;


//todo 待处理
@Service
public class SiteServiceImpl implements SiteService
{
    private static Logger LOGGER = LoggerFactory.getLogger(SiteServiceImpl.class);
    @Resource
    private CommentVoMapper commentDao;
    @Resource
    private AttachVoMapper attachDao;
    @Resource
    private ContentVoMapper contentDao;
    @Resource
    private MetaVoMapper metaDao;

    //todo service实现
    //    获取评论消息
    @Override
    public List<CommentVo> recentComments(int limit)
    {
        LOGGER.info("Enter recentComments method:limit={}", limit);
        if (limit < 0 || limit > 10) limit = 10;
        CommentVoExample example = new CommentVoExample();
        example.setOrderByClause("created desc");
        PageHelper.startPage(1, limit);
        List<CommentVo> byPage = commentDao.selectByExampleWithBLOBs(example);
        LOGGER.info("Exit recentComments method");
        return byPage;
    }

    //    获取评论内容
    @Override
    public List<ContentVo> recentContents(int limit)
    {
        LOGGER.debug("Enter recentContent method");
        if (limit < 0 || limit > 10) limit = 10;
        ContentVoExample example = new ContentVoExample();
        example.createCriteria().andStatusEqualTo(Types.PUBLISH.getType()).andTypeEqualTo(Types.ARTICLE.getType());
        example.setOrderByClause("created desc");
        List<ContentVo> list = contentDao.selectByExample(example);
        LOGGER.debug("Exit recentContents method");
        return list;
    }

    @Override
    public CommentVo getComment(Integer coid)
    {
        if (coid != null) return commentDao.selectByPrimaryKey(coid);
        return null;
    }

    //    备用文件写入
    @Override
    public BackResponseBo backup(String bk_type, String bk_path, String fmt) throws Exception
    {
        BackResponseBo backResponse = new BackResponseBo();
        if (bk_path.equals("attach"))
        {
            if (StringUtils.isNotBlank(bk_path))
            {
                throw new TipException("请输入备份文件的存储路径！");
            }
            if (!(new File(bk_path)).isDirectory()) throw new TipException("请输入一个存在的目录！");
            String bkAttachDir = AttachController.CLASSPATH + "upload/";
            String bkThemesDir = AttachController.CLASSPATH + "templates/themes";
            String fname = DateKit.dateFormat(new Date(), fmt) + "_" + TaleUtils.getRandomNumber(5) + ".zip";
            String attachPath = bk_path + "/" + "attachs_" + fname;
            String themesPaht = bk_path + "/" + "themes_" + fname;
            ZipUtils.zipFolder(bkAttachDir, attachPath);
            ZipUtils.zipFolder(bkThemesDir, themesPaht);

            backResponse.setAttachPath(attachPath);
            backResponse.setThemePath(themesPaht);


            if (!(new File(bkAttachDir).isDirectory()))
            {
                File file = new File(bkAttachDir);
                if (!file.exists()) file.mkdirs();
            }
        }
        if (bk_type.equals("db"))
        {
            String bkAttachDir = AttachController.CLASSPATH + "upload/";
            if (!(new File(bkAttachDir)).isDirectory())
            {
                File file = new File(bkAttachDir);
                if (!file.exists()) file.mkdirs();
            }
            String sqlFileName = "tale_" + DateKit.dateFormat(new Date(), fmt) + "_" + TaleUtils.getRandomNumber(5) + ".sql";
            String zipFile = sqlFileName.replace(".sql", ".zip");

            Backup backUp = new Backup(TaleUtils.getNewDataSource().getConnection());
            String sqlContent = backUp.execute();

            File sqlFile = new File(bkAttachDir + sqlFileName);
            write(sqlContent, sqlFile, Charset.forName("UTF-8"));

            String zip = bkAttachDir + zipFile;
            ZipUtils.zipFile(sqlFile.getPath(), zip);

            if (!sqlFile.exists()) throw new TipException("数据库备份失败");
            sqlFile.delete();
            backResponse.setSqlPath(zipFile);

            //            10秒后删除文件
            new Timer().schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    new File(zip).delete();
                }
            }, 10 * 1000);
        }
        return backResponse;

    }

    private void write(String data, File file, Charset charset)
    {
        FileOutputStream os = null;
        try
        {
            os = new FileOutputStream(file);
            os.write(data.getBytes(charset));
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        } finally
        {
            if (os != null) try
            {
                os.close();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    //    获得统计信息
    @Override
    public StatisticsBo getStatistics()
    {
        LOGGER.info("Enter getStatistics method");
        StatisticsBo statistics = new StatisticsBo();

        ContentVoExample contentVoExample = new ContentVoExample();
        contentVoExample.createCriteria().andTypeEqualTo(Types.ARTICLE.getType()).andStatusEqualTo(Types.PUBLISH.getType());
        Long articles = contentDao.countByExample(contentVoExample);

        Long comments = commentDao.countByExample(new CommentVoExample());

        Long attachs = attachDao.countByExample(new AttachVoExample());

        MetaVoExample metaVoExample = new MetaVoExample();
        metaVoExample.createCriteria().andTypeEqualTo(Types.LINK.getType());
        Long links = metaDao.countByExample(metaVoExample);

        statistics.setArticles(articles);
        statistics.setComments(comments);
        statistics.setAttachs(attachs);
        statistics.setLinks(links);

        LOGGER.debug("Exit getStatistics method");
        return statistics;
    }


    @Override
    public List<ArchiveBo> getArchives()
    {
        LOGGER.debug("Enter getArchives method");
        List<ArchiveBo> archives = contentDao.findReturnArchiveBo();
        if (archives != null)
        {
            archives.forEach(archive -> {
                ContentVoExample example = new ContentVoExample();
                ContentVoExample.Criteria criteria = example.createCriteria().andTypeEqualTo(Types.ARTICLE.getType()).andTypeEqualTo(Types.PUBLISH.getType());
                example.setOrderByClause("created desc");
                String date = archive.getDate();
                Date sd = DateKit.dateFormat(date, "yyyy年MM月");
                int start = DateKit.getUnixTimeByDate(sd);
                int end = DateKit.getUnixTimeByDate(DateKit.dateAdd(DateKit.INTERVAL_MONTH, sd, -1)) - 1;
                criteria.andCreatedGreaterThan(start);
                criteria.andCreatedLessThan(end);
                List<ContentVo> contentVos = contentDao.selectByExample(example);
                archive.setArticles(contentVos);
            });
        }
        LOGGER.debug("Exit getArchives method");
        return archives;
    }

    @Override
    public List<MetaDto> metas(String type, String orderBy, int limit)
    {
        LOGGER.debug("Enter metas method:type={},order={},limit={}", type, orderBy, limit);
        List<MetaDto> retList = null;
        if (StringUtils.isNotBlank(type))
        {
            if (StringUtils.isBlank(orderBy))
                orderBy = "count desc,a,mid desc";
            if (limit < 1 || limit > WebConst.MAX_POSTS)
                limit = 10;
            Map<String, Object> paraMap = new HashMap<>();
            paraMap.put("type", type);
            paraMap.put("order", orderBy);
            paraMap.put("limit", limit);
            retList = metaDao.selectFromSql(paraMap);
        }
        LOGGER.debug("Exit metas method");
        return retList;
    }
}
