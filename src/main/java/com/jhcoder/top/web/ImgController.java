package com.jhcoder.top.web;

import com.jhcoder.top.entity.ImgEntity;
import com.jhcoder.top.mapper.ImgEntityMapper;
import com.jhcoder.top.mapper.PageBean;
import com.jhcoder.top.service.ImgService;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class ImgController {

    @Autowired
    private ImgService imgService;

    @Autowired
    private Environment env;

    static Set<String>  picType = new HashSet<>();
    static {
        picType.addAll(Arrays.asList("jpg","png","gif","ico"));
    }

    @RequestMapping("/")
    public String index() {
        return "upload";
    }

    @GetMapping("/list")
    public String list() {
        return "list";
    }

    @GetMapping("/delete")
    @ResponseBody
    public String delete(int id) {
       ImgEntity imgEntity =  imgService.selectByPrimaryKey(id);
       File file = new File(imgEntity.getPath());
       if(file.exists()) {
           file.delete();
           imgService.deleteByPrimaryKey(id);
       }
        return "success";
    }

    @GetMapping("/pagedata")
    @ResponseBody
    public PageBean pagedata(int pageNum, int pageSize) {
        PageBean<ImgEntity> pageBean = this.imgService.selectPage(pageNum, pageSize);
        return pageBean;
    }

    /** 
    * @Description: base64复制粘贴上传
    * @Param: [base64, name, redirectAttributes, request] 
    * @return: java.lang.String 
    * @Date: 2018/6/9 
    */ 
    @PostMapping("/upload_base64") // //new annotation since 4.3
    @ResponseBody
    public String uploadBase64(@RequestParam("file") String base64,String name,
                                   RedirectAttributes redirectAttributes, HttpServletRequest request) {
        try {
            String dataPrix = "";
            String data = "";

            if (base64 == null || "".equals(base64)) {
                throw new Exception("上传失败，上传图片数据为空");
            } else {
                String[] d = base64.split("base64,");
                if (d != null && d.length == 2) {
                    dataPrix = d[0];
                    data = d[1];
                } else {
                    throw new Exception("上传失败，数据不合法");
                }
            }
            //判断图片类型
            String suffix = "";
            if("data:image/jpeg;".equalsIgnoreCase(dataPrix)){//data:image/jpeg;base64,base64编码的jpeg图片数据
                suffix = ".jpg";
            } else if("data:image/x-icon;".equalsIgnoreCase(dataPrix)){//data:image/x-icon;base64,base64编码的icon图片数据
                suffix = ".ico";
            } else if("data:image/gif;".equalsIgnoreCase(dataPrix)){//data:image/gif;base64,base64编码的gif图片数据
                suffix = ".gif";
            } else if("data:image/png;".equalsIgnoreCase(dataPrix)){//data:image/png;base64,base64编码的png图片数据
                suffix = ".png";
            }else{
                throw new Exception("上传图片格式不合法");
            }
            String filename = getRandomFileName() + suffix;
            byte[] bs = Base64Utils.decodeFromString(data);
            String folder = getyyyyMM();
            //fullpath
            String fullpath = env.getProperty("img.fullpath") + "/" + folder + "/" + filename;
            //localpath
            String localpath = env.getProperty("img.localpath")+ "/" + folder+"/"+ filename;

            FileUtils.writeByteArrayToFile(new File(localpath), bs);

            //保存数据
            ImgEntity imgEntity = new ImgEntity();
            imgEntity.setFullpath(fullpath);
            imgEntity.setName(filename);
            imgEntity.setSize((long) (bs.length / 1000));
            imgEntity.setCreat(new Date());
            imgEntity.setPath(localpath);
            imgEntity.setIp(getIpAddr(request));
            imgService.insert(imgEntity);

            return fullpath;
        }catch (Exception E) {
            return "失败: " + E.getMessage();
        }
    }


    /**
    * @Description:  spring正常上传
    * @Param: [file, name, request]
    * @return: java.util.Map<java.lang.String,java.lang.String>
    * @Date: 2018/6/9
    */
    @PostMapping("/upload") // //new annotation since 4.3
    @ResponseBody
    public Map<String, String> singleFileUpload(@RequestParam("file") MultipartFile file,String name, HttpServletRequest request) {
        Map<String, String> result = new HashMap<>();
        if (file.isEmpty()) {
           // redirectAttributes.addFlashAttribute("message", "没有选择文件");
            result.put("msg","没有选择文件");
            return result;
        }
        //文件后缀
        String suffix  = getExtensionName(file.getOriginalFilename());
        if(StringUtils.isEmpty(suffix) || !picType.contains(suffix)){
            result.put("msg","文件类型不允许");
            return result;
        }
        //文件名字
        String filename="";
        if(!StringUtils.isEmpty(name)) {
            filename = name + "." + suffix;
        }else {
            filename = file.getOriginalFilename();
        }

        filename = filename.substring(0, filename.indexOf(suffix)-1) +"_"+ System.currentTimeMillis() +"."+ suffix;
        String folder = getyyyyMM();
        //fullpath
        String fullpath = env.getProperty("img.fullpath") + "/" + folder + "/" + filename;
        //localpath
        String localpath = env.getProperty("img.localpath")+ "/" + folder+"/"+ filename;

        try {
            // Get the file and save it somewhere
            byte[] bytes = file.getBytes();
            //Path path = Paths.get(localpath);
           // Files.write(path, bytes);
            FileUtils.writeByteArrayToFile(new File(localpath), bytes);

            //保存数据
            ImgEntity imgEntity = new ImgEntity();
            imgEntity.setFullpath(fullpath);
            imgEntity.setName(filename);
            imgEntity.setSize(file.getSize());
            imgEntity.setCreat(new Date());
            imgEntity.setPath(localpath);
            imgEntity.setIp(getIpAddr(request));
            imgService.insert(imgEntity);

        } catch (Exception e) {
            e.printStackTrace();
            result.put("msg","文件上传失败");
            return result;
        }
        result.put("msg",fullpath);
        return result;
    }

    /** 
    * @Description: 获取文件后缀 
    * @Param: [filename] 
    * @return: java.lang.String 
    * @Date: 2018/6/9 
    */ 
    public static String getExtensionName(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot >-1) && (dot < (filename.length() - 1))) {
                return filename.substring(dot + 1);
            }
        }
        return filename;
    }

    /** 
    * @Description: 随机文件名 
    * @Param: [] 
    * @return: java.lang.String 
    * @Date: 2018/6/9 
    */ 
    public String getRandomFileName() {
        return System.currentTimeMillis()+"";
    }

    /** 
    * @Description: 获取文件夹名 
    * @Param: [] 
    * @return: java.lang.String 
    * @Date: 2018/6/9 
    */ 
    public String getyyyyMM() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
        return sdf.format(new Date());
    }

    /** 
    * @Description: 记录ip
    * @Param: [request] 
    * @return: java.lang.String 
    * @Date: 2018/6/9 
    */ 
    public static String getIpAddr(HttpServletRequest request) throws Exception{
        String ip = request.getHeader("X-Real-IP");
        if (!StringUtils.isEmpty(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        ip = request.getHeader("X-Forwarded-For");
        if (!StringUtils.isEmpty(ip) && !"unknown".equalsIgnoreCase(ip)) {
        // 多次反向代理后会有多个IP值，第一个为真实IP。
            int index = ip.indexOf(',');
            if (index != -1) {
                return ip.substring(0, index);
            } else {
                return ip;
            }
        } else {
            return request.getRemoteAddr();
        }
    }
}
