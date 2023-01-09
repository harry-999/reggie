package com.hhl.reggie.controller;

import com.hhl.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.UUID;

@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {

    @Value("${reggie.path}")
    private String basePath;

    /**
     * 文件上传
     * @param file
     * @return
     */
//    根据前端页面page/demo/upload.html中表示文件上传的参数名字为file所以这里的形参名字不能变固定为file
//    返回值为什么要返回文件名，因为我们需要将文件上传的文件名储存到数据库中，方便使用
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) throws IOException {

        //此时file为暂时的临时文件需要将其转存到服务器中
        String originalFilename = file.getOriginalFilename();
        //后缀名
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        //利用UUID重新生成文件名，防止文件名称重复导致文件覆盖
        String fileName = UUID.randomUUID().toString() + suffix;
        //创建一个目录对象是否存在
        File dir = new File(basePath);
        if(!dir.exists()){
            //目录不存在,则创建目录
            dir.mkdirs();
        }
        file.transferTo(new File(basePath+fileName));

        return R.success(fileName);
    }

//    根据前端代码中img标签表示需要下载文件(回显)，img标签请求表示的是get请求方式，请求中的参数名称为name 需要响应给客户端文件数据所以要用HttpServletResponse
    @GetMapping("download")
    public void download(String name, HttpServletResponse response){

        try {
            //输入流，通过输入流读取文件内容
            FileInputStream fileInputStream = new FileInputStream(basePath + name);

            //输出流，通过输出流将文件写回浏览器，在浏览器中显示图片
            ServletOutputStream outputStream = response.getOutputStream();
            //给响应设置类型
            response.setContentType("image/jpeg");

            int length=0;
            byte[] bytes = new byte[1024];
            while((length=fileInputStream.read(bytes))!=-1){
                outputStream.write(bytes,0,length);
                outputStream.flush();
            }
            //关闭资源
            outputStream.close();
            fileInputStream.close();


        } catch (Exception e) {
            e.printStackTrace();
        }



    }
}
