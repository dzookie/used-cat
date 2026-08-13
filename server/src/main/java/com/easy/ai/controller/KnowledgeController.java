package com.easy.ai.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.easy.ai.common.PageBean;
import com.easy.ai.common.Result;
import com.easy.ai.entity.KnowledgeDoc;
import com.easy.ai.service.KnowledgeDocService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/knowledge")
@Tag(name = "知识库管理", description = "后台知识库文档管理接口")
@Slf4j
public class KnowledgeController {

    @Autowired
    private KnowledgeDocService knowledgeDocService;

    @Autowired
    private VectorStore vectorStore;

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${spring.ai.vectorstore.qdrant.host:localhost}")
    private String qdrantHost;

    @Value("${spring.ai.vectorstore.qdrant.rest-port:6333}")
    private int qdrantRestPort;

    @Value("${spring.ai.vectorstore.qdrant.collection-name:vector_store}")
    private String qdrantCollection;

    @Value("${spring.servlet.multipart.max-file-size:10MB}")
    private String maxFileSize;

    @GetMapping("/admin/list")
    @Operation(summary = "后台分页查询知识库文档", description = "支持按标题和类型搜索")
    public Result<PageBean<KnowledgeDoc>> adminList(
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "type", required = false) String type) {

        Page<KnowledgeDoc> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<KnowledgeDoc> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(KnowledgeDoc::getTitle, keyword);
        }
        if (type != null && !type.trim().isEmpty()) {
            wrapper.eq(KnowledgeDoc::getType, type);
        }

        wrapper.orderByDesc(KnowledgeDoc::getCreateTime);

        Page<KnowledgeDoc> result = knowledgeDocService.page(page, wrapper);
        return Result.success("查询成功", new PageBean<>(result.getTotal(), result.getRecords()));
    }

    @PostMapping("/admin/addText")
    @Operation(summary = "新增文本知识", description = "管理员新增纯文本知识文档")
    public Result<KnowledgeDoc> addText(@RequestBody KnowledgeDoc doc) {
        if (doc.getTitle() == null || doc.getTitle().trim().isEmpty()) {
            return Result.error("标题不能为空");
        }
        if (doc.getContent() == null || doc.getContent().trim().isEmpty()) {
            return Result.error("内容不能为空");
        }

        doc.setType("text");
        doc.setVectorStatus(0);
        doc.setChunkCount(0);
        doc.setCreateTime(LocalDateTime.now());
        doc.setUpdateTime(LocalDateTime.now());

        knowledgeDocService.save(doc);
        return Result.success("新增成功", doc);
    }

    @PostMapping("/admin/uploadPdf")
    @Operation(summary = "上传PDF知识文档", description = "管理员上传PDF文件，暂存文件并记录元信息")
    public Result<KnowledgeDoc> uploadPdf(
            @Parameter(description = "PDF文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "文档标题") @RequestParam(value = "title", required = false) String title) {

        if (file == null || file.isEmpty()) {
            return Result.error("请选择文件");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
            return Result.error("仅支持PDF格式文件");
        }

        if (title == null || title.trim().isEmpty()) {
            title = originalFilename.replace(".pdf", "").replace(".PDF", "");
        }

        try {
            String projectPath = System.getProperty("user.dir");
            String uploadDir = projectPath + "/server/upload/knowledge";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String fileName = UUID.randomUUID().toString().replace("-", "") + ".pdf";
            File dest = new File(uploadDir + "/" + fileName);
            file.transferTo(dest);

            KnowledgeDoc doc = new KnowledgeDoc();
            doc.setTitle(title.trim());
            doc.setType("pdf");
            doc.setContent("PDF文件已上传，待解析");
            doc.setVectorStatus(0);
            doc.setChunkCount(0);
            doc.setFilePath("/upload/knowledge/" + fileName);
            doc.setFileSize(file.getSize());
            doc.setCreateTime(LocalDateTime.now());
            doc.setUpdateTime(LocalDateTime.now());

            knowledgeDocService.save(doc);
            return Result.success("上传成功", doc);
        } catch (IOException e) {
            return Result.error("文件上传失败：" + e.getMessage());
        }
    }

    @PutMapping("/admin/update")
    @Operation(summary = "更新知识文档", description = "仅支持更新文本类型的标题和内容")
    public Result<Void> update(@RequestBody KnowledgeDoc doc) {
        if (doc.getId() == null) {
            return Result.error("ID不能为空");
        }

        KnowledgeDoc existing = knowledgeDocService.getById(doc.getId());
        if (existing == null) {
            return Result.error("文档不存在");
        }

        if (!"text".equals(existing.getType())) {
            return Result.error("PDF类型文档不支持直接编辑内容");
        }

        if (doc.getTitle() != null && !doc.getTitle().trim().isEmpty()) {
            existing.setTitle(doc.getTitle().trim());
        }
        if (doc.getContent() != null) {
            existing.setContent(doc.getContent());
        }
        existing.setVectorStatus(0);
        existing.setChunkCount(0);
        existing.setUpdateTime(LocalDateTime.now());

        knowledgeDocService.updateById(existing);
        return Result.success("更新成功");
    }

    @DeleteMapping("/admin/delete/{id}")
    @Operation(summary = "删除知识文档", description = "删除知识文档及关联文件和向量数据")
    public Result<Void> delete(@PathVariable("id") Integer id) {
        if (id == null) {
            return Result.error("ID不能为空");
        }

        KnowledgeDoc doc = knowledgeDocService.getById(id);
        if (doc == null) {
            return Result.error("文档不存在");
        }

        // 删除关联的PDF文件
        if (doc.getFilePath() != null) {
            String projectPath = System.getProperty("user.dir");
            File file = new File(projectPath + "/server" + doc.getFilePath());
            if (file.exists()) {
                file.delete();
            }
        }

        // 从Qdrant删除关联的向量数据
        if (doc.getVectorStatus() != null && doc.getVectorStatus() == 1) {
            try {
                String url = String.format("http://%s:%d/collections/%s/points/delete",
                        qdrantHost, qdrantRestPort, qdrantCollection);
                Map<String, Object> filter = Map.of(
                        "must", new Object[]{
                                Map.of("key", "docId",
                                        "match", Map.of("value", id))
                        }
                );
                Map<String, Object> body = Map.of("filter", filter);
                log.info("删除Qdrant向量请求: URL={}, body={}", url, body);
                var response = restTemplate.postForEntity(url, body, String.class);
                log.info("删除Qdrant向量响应: status={}, body={}",
                        response.getStatusCode(), response.getBody());
                log.info("已从Qdrant删除文档[id={}]的向量数据", id);
            } catch (Exception e) {
                log.error("删除Qdrant向量数据失败", e);
            }
        }

        knowledgeDocService.removeById(id);
        return Result.success("删除成功");
    }

    @GetMapping("/admin/{id}")
    @Operation(summary = "获取知识文档详情", description = "根据ID获取知识文档详情")
    public Result<KnowledgeDoc> getById(@PathVariable("id") Integer id) {
        KnowledgeDoc doc = knowledgeDocService.getById(id);
        if (doc != null) {
            return Result.success("查询成功", doc);
        }
        return Result.error("文档不存在");
    }

    @PostMapping("/admin/vectorize/{id}")
    @Operation(summary = "向量化文档", description = "将文档内容切片并向量化存入向量数据库")
    public Result<Map<String, Object>> vectorize(@PathVariable("id") Integer id) {
        if (id == null) {
            return Result.error("ID不能为空");
        }

        KnowledgeDoc doc = knowledgeDocService.getById(id);
        if (doc == null) {
            return Result.error("文档不存在");
        }

        try {
            List<Document> documents;

            if ("text".equals(doc.getType())) {
                Map<String, Object> meta = new HashMap<>();
                meta.put("docId", doc.getId());
                meta.put("title", doc.getTitle());
                meta.put("type", doc.getType());
                Document textDoc = new Document(doc.getContent(), meta);
                documents = List.of(textDoc);
            } else if ("pdf".equals(doc.getType())) {
                if (doc.getFilePath() == null) {
                    return Result.error("PDF文件路径不存在");
                }
                String projectPath = System.getProperty("user.dir");
                File pdfFile = new File(projectPath + "/server" + doc.getFilePath());
                if (!pdfFile.exists()) {
                    return Result.error("PDF文件不存在");
                }

                PagePdfDocumentReader reader = new PagePdfDocumentReader(new FileSystemResource(pdfFile));
                List<Document> pdfDocs = reader.get();
                documents = new ArrayList<>();
                for (Document d : pdfDocs) {
                    Map<String, Object> meta = new HashMap<>(d.getMetadata());
                    meta.put("docId", doc.getId());
                    meta.put("title", doc.getTitle());
                    meta.put("type", doc.getType());
                    documents.add(new Document(d.getText(), meta));
                }
            } else {
                return Result.error("不支持的文档类型");
            }

            TokenTextSplitter splitter = new TokenTextSplitter();
            List<Document> chunks = splitter.split(documents);

            List<Document> finalChunks = new ArrayList<>();
            for (Document c : chunks) {
                Map<String, Object> meta = new HashMap<>(c.getMetadata());
                meta.put("docId", doc.getId());
                meta.put("title", doc.getTitle());
                finalChunks.add(new Document(c.getText(), meta));
            }

            vectorStore.accept(finalChunks);

            doc.setVectorStatus(1);
            doc.setChunkCount(finalChunks.size());
            doc.setUpdateTime(LocalDateTime.now());
            knowledgeDocService.updateById(doc);

            Map<String, Object> result = new HashMap<>();
            result.put("chunkCount", finalChunks.size());
            result.put("vectorStatus", 1);

            return Result.success("向量化成功", result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("向量化失败：" + e.getMessage());
        }
    }

    @GetMapping("/admin/chunk/check")
    @Operation(summary = "检查文件已上传分片", description = "根据文件MD5检查已上传的分片列表，用于断点续传")
    public Result<Map<String, Object>> checkChunk(
            @Parameter(description = "文件MD5") @RequestParam("fileMd5") String fileMd5,
            @Parameter(description = "文件名") @RequestParam("fileName") String fileName) {

        if (fileMd5 == null || fileMd5.trim().isEmpty()) {
            return Result.error("文件MD5不能为空");
        }

        Map<String, Object> result = new HashMap<>();
        String projectPath = System.getProperty("user.dir");
        String chunkDir = projectPath + "/server/upload/chunks/" + fileMd5;
        File dir = new File(chunkDir);

        List<Integer> uploadedChunks = new ArrayList<>();
        boolean uploaded = false;

        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((d, name) -> name.endsWith(".chunk"));
            if (files != null) {
                for (File f : files) {
                    String name = f.getName();
                    int idx = name.lastIndexOf(".chunk");
                    if (idx > 0) {
                        try {
                            uploadedChunks.add(Integer.parseInt(name.substring(0, idx)));
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }
            Collections.sort(uploadedChunks);
        }

        result.put("uploadedChunks", uploadedChunks);
        result.put("uploaded", uploaded);
        return Result.success("查询成功", result);
    }

    @PostMapping("/admin/chunk/upload")
    @Operation(summary = "上传单个分片", description = "上传文件的一个分片")
    public Result<String> uploadChunk(
            @Parameter(description = "分片文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "文件MD5") @RequestParam("fileMd5") String fileMd5,
            @Parameter(description = "分片索引") @RequestParam("chunkIndex") Integer chunkIndex,
            @Parameter(description = "总分片数") @RequestParam("chunkTotal") Integer chunkTotal) {

        if (file == null || file.isEmpty()) {
            return Result.error("分片文件不能为空");
        }
        if (fileMd5 == null || fileMd5.trim().isEmpty()) {
            return Result.error("文件MD5不能为空");
        }
        if (chunkIndex == null || chunkIndex < 0) {
            return Result.error("分片索引无效");
        }

        try {
            String projectPath = System.getProperty("user.dir");
            String chunkDir = projectPath + "/server/upload/chunks/" + fileMd5;
            File dir = new File(chunkDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File chunkFile = new File(chunkDir + "/" + chunkIndex + ".chunk");
            file.transferTo(chunkFile);

            return Result.success("分片上传成功", String.valueOf(chunkIndex));
        } catch (IOException e) {
            return Result.error("分片上传失败：" + e.getMessage());
        }
    }

    @PostMapping("/admin/chunk/merge")
    @Operation(summary = "合并分片并创建文档", description = "合并所有分片为完整PDF文件并创建知识库文档记录")
    public Result<KnowledgeDoc> mergeChunk(
            @Parameter(description = "文件MD5") @RequestParam("fileMd5") String fileMd5,
            @Parameter(description = "文件名") @RequestParam("fileName") String fileName,
            @Parameter(description = "文档标题") @RequestParam(value = "title", required = false) String title,
            @Parameter(description = "文件大小") @RequestParam("fileSize") Long fileSize) {

        if (fileMd5 == null || fileMd5.trim().isEmpty()) {
            return Result.error("文件MD5不能为空");
        }
        if (fileName == null || !fileName.toLowerCase().endsWith(".pdf")) {
            return Result.error("仅支持PDF格式文件");
        }
        if (fileSize == null || fileSize <= 0) {
            return Result.error("文件大小无效");
        }

        if (title == null || title.trim().isEmpty()) {
            title = fileName.replace(".pdf", "").replace(".PDF", "");
        }

        String projectPath = System.getProperty("user.dir");
        String chunkDir = projectPath + "/server/upload/chunks/" + fileMd5;
        File dir = new File(chunkDir);

        if (!dir.exists() || !dir.isDirectory()) {
            return Result.error("分片目录不存在");
        }

        File[] chunkFiles = dir.listFiles((d, name) -> name.endsWith(".chunk"));
        if (chunkFiles == null || chunkFiles.length == 0) {
            return Result.error("没有找到分片文件");
        }

        try {
            Arrays.sort(chunkFiles, (a, b) -> {
                int idxA = Integer.parseInt(a.getName().replace(".chunk", ""));
                int idxB = Integer.parseInt(b.getName().replace(".chunk", ""));
                return idxA - idxB;
            });

            String uploadDir = projectPath + "/server/upload/knowledge";
            File knowledgeDir = new File(uploadDir);
            if (!knowledgeDir.exists()) {
                knowledgeDir.mkdirs();
            }

            String newFileName = UUID.randomUUID().toString().replace("-", "") + ".pdf";
            File destFile = new File(uploadDir + "/" + newFileName);

            try (FileOutputStream fos = new FileOutputStream(destFile)) {
                byte[] buffer = new byte[8192];
                for (File chunkFile : chunkFiles) {
                    try (FileInputStream fis = new FileInputStream(chunkFile)) {
                        int len;
                        while ((len = fis.read(buffer)) != -1) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
            }

            KnowledgeDoc doc = new KnowledgeDoc();
            doc.setTitle(title.trim());
            doc.setType("pdf");
            doc.setContent("PDF文件已上传，待解析");
            doc.setVectorStatus(0);
            doc.setChunkCount(0);
            doc.setFilePath("/upload/knowledge/" + newFileName);
            doc.setFileSize(fileSize);
            doc.setCreateTime(LocalDateTime.now());
            doc.setUpdateTime(LocalDateTime.now());

            knowledgeDocService.save(doc);

            for (File chunkFile : chunkFiles) {
                chunkFile.delete();
            }
            dir.delete();

            return Result.success("合并成功", doc);
        } catch (IOException e) {
            return Result.error("合并失败：" + e.getMessage());
        }
    }
}