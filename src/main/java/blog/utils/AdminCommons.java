package blog.utils;

import blog.model.Vo.MetaVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public final class AdminCommons
{
    public static boolean exist_cat(MetaVo category, String cats)
    {
        String[] arr = StringUtils.split(cats, ",");
        if (arr != null && arr.length > 0)
        {
            for (String c : arr)
                if (c.trim().equals(category.getName())) return true;
        }
        return false;
    }

    private static final String[] COLORS = {"default", "primary", "success", "info", "warning", "danger", "inverse", "purple", "pink"};

    public static String rand_color()
    {
        Random random = new Random();
        //        int r=random.nextInt(9)%9;
        int r = Tools.rand(0, COLORS.length - 1);
        return COLORS[r];
    }

}
