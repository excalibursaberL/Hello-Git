import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MarkdownToHtml {
    //匹配粗体
    static final String Markdown_Bold = "([\\*_]{2})(.*?)\\1";
    //匹配斜体
    static final String Markdown_Italic = "(?<![\\*_])(\\*|_)([^\\*_]+?)\\1";
    //匹配删除线
    static final String Markdown_Delete_Line="(~~)(.*?)\\1";
    //匹配非图片链接
    static final String Markdown_Link="(?<!!)\\[(.*?)\\]\\((.*?)\\)";
    //匹配行内代码
    static final String Markdown_Single_Line_Code="(?<!`)(`)([^`]+?)`(?!`)";

    //匹配标题文本
    static final String Markdown_Title="(#{1,6})(.*)";
    //匹配图片链接
    static final String Markdown_IMAGE_LINK="^\\!\\[(.*?)\\]\\((.*?)\\)$";
    //匹配分割线
    static final String Markdown_Separate_Line ="^(\\*|-|_){3}$";

    //匹配无序列表
    static final String Markdown_Unordered_List="^(-|\\+|\\*)\\s(.*)";
    //匹配有序列表
    static final String Markdown_Ordered_List="^([\\d+])\\.\\s(.*)";
    //匹配表格
    static final String Markdown_TABLE_Group="^(\\|)(.*?)(\\|)$";
    //匹配引用
    static final String Markdown_Quote="^([>]\\s?)(.*)";

    //匹配多行代码块
    static final String Markdown_Code_Block_Start="^```\\w+";
    static final String Markdown_Code_Block_End="^```$";
    //([\s\S]*?[\w\W]*?|[\d\D]*?)均表示匹配任意字符
    static final String Markdown_Code="(```)(\\w+)([\\s\\S]*?[\\w\\W]*?|[\\d\\D]*?)\\1";

    public static void convert(String mdFilepath,String htmlFilepath) {
         List<String> md_file=readMd(mdFilepath);

         String new_line="";
         List<String>temp_list=new ArrayList<>();
         List<String> result_list=new ArrayList<>();

         for (int i = 0; i < md_file.size(); i++) {
             String line=md_file.get(i);
             Matcher bold_matcher=Pattern.compile(Markdown_Bold).matcher(line);
             if (bold_matcher.find()) {
                 line=replaceBold(line);
             }

             Matcher italic_matcher=Pattern.compile(Markdown_Italic).matcher(line);
             if (italic_matcher.find()) {
                 line=replaceItalic(line);
             }

             Matcher delete_matcher=Pattern.compile(Markdown_Delete_Line).matcher(line);
             if (delete_matcher.find()) {
                 line=replaceDeleteLine(line);
             }

             Matcher link_matcher=Pattern.compile(Markdown_Link).matcher(line);
             if (link_matcher.find()) {
                 line=replaceLink(line);
             }

             Matcher single_line_code_matcher=Pattern.compile(Markdown_Single_Line_Code).matcher(line);
             if (single_line_code_matcher.find()) {
                 line=replaceSingleLineCode(line);
             }

             Matcher title_matcher=Pattern.compile(Markdown_Title).matcher(line);
             if (title_matcher.find()) {
                 line=replaceTitle(line);
             }

             Matcher image_link_matcher=Pattern.compile(Markdown_IMAGE_LINK).matcher(line);
             if (image_link_matcher.find()) {
                 line=replaceImageLink(line);
             }

             Matcher seprate_matcher=Pattern.compile(Markdown_Separate_Line).matcher(line);
             if(seprate_matcher.find()) {
                 line=replaceSeparateLine(line);
             }

             Matcher unordered_list_matcher=Pattern.compile(Markdown_Unordered_List).matcher(line);
             while (unordered_list_matcher.find()) {
                 temp_list.add(line);
                 i++;
                 line=md_file.get(i);
                 unordered_list_matcher=Pattern.compile(Markdown_Unordered_List).matcher(line);
             }
             result_list.add(replaceUnorderedList(temp_list));
             temp_list.clear();

             Matcher ordered_list_matcher=Pattern.compile(Markdown_Ordered_List).matcher(line);
             while (ordered_list_matcher.find()) {
                 temp_list.add(line);
                 i++;
                 line=md_file.get(i);
                 ordered_list_matcher=Pattern.compile(Markdown_Ordered_List).matcher(line);
             }
             result_list.add(replaceOrderedList(temp_list));
             temp_list.clear();

             Matcher quote_matcher=Pattern.compile(Markdown_Quote).matcher(line);
             while (quote_matcher.find()) {
                 temp_list.add(line);
                 i++;
                 line=md_file.get(i);
                 quote_matcher=Pattern.compile(Markdown_Quote).matcher(line);
             }
             result_list.add(replaceQuote(temp_list));
             temp_list.clear();

             Matcher table_matcher=Pattern.compile(Markdown_TABLE_Group).matcher(line);
             while (table_matcher.find()) {
                 temp_list.add(line);
                 i++;
                 line=md_file.get(i);
                 table_matcher=Pattern.compile(Markdown_TABLE_Group).matcher(line);
             }
             result_list.add(replaceTable(temp_list));
             temp_list.clear();

             Matcher code_block_start_matcher=Pattern.compile(Markdown_Code_Block_Start).matcher(line);
             while (code_block_start_matcher.find()) {
                 Matcher  code_block_end_matcher=Pattern.compile(Markdown_Code_Block_End).matcher(line);
                 if (!code_block_end_matcher.find()) {
                     temp_list.add(line);
                     i++;
                     line=md_file.get(i);
                     code_block_end_matcher=Pattern.compile(Markdown_Code_Block_End).matcher(line);
                 }
                 temp_list.add(line);
                 result_list.add(replaceCodeBlock(temp_list));
                 continue;
             }

             if(line.trim().length()>0){
                 result_list.add("<p>"+line+"</p>");
             }
         }

         StringBuilder html_file=new StringBuilder();
         html_file.append("<!DOCTYPE html>\n"+
                 "<html lang=\"en\">\n"+
                 "<head>\n" +
                 "    <meta charset=\"UTF-8\">\n" +
                 "    <title>Title</title>\n"+
                 "</head>>\n"+"<body>\n");

         for(String line:result_list){
             html_file.append(line);
         }
         html_file.append("</body>\n</html>");

         writeHtml(htmlFilepath,html_file.toString());
    }

    private static void writeHtml(String htmlFilepath,String html_file) {
        FileWriter fw=null;
        try {
            File file=new File(htmlFilepath);
            fw=new FileWriter(file);
            fw.write(html_file);
        }catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if (fw != null) {
                    fw.close();
                }
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String replaceCodeBlock(List<String> list) {
        StringBuffer sb=new StringBuffer();
        if (list.size() > 0) {
            sb.append("<pre><code>\n");
            //最后一行是代码的结束标记，不访问
            for (int i = 0; i < list.size()-1; i++) {
                sb.append(list.get(i)).append("\n");
            }
            sb.append("</code></pre>\n");
        }
        return sb.toString();
    }

    private static String replaceTable(List<String> list) {
        StringBuffer sb=new StringBuffer();
        if (list.size() >0) {
            sb.append("<table border='1' cellspacing='0'>");
            for (int i = 0; i < list.size(); i++) {
                String line=list.get(i);
                Matcher table_matcher=Pattern.compile(Markdown_TABLE_Group).matcher(line);
                if (table_matcher.find()) {
                    String row=table_matcher.group(2);
                    String[] cols=row.split("//|");
                    sb.append("<tr>");
                    for(String col:cols)
                    {
                        if(i==0){
                            sb.append("<th>").append(col).append("</th>");
                        }
                        else{
                            sb.append("<td>").append(col).append("</td>");
                        }
                    }
                    sb.append("</tr>");
                }
            }
            sb.append("</table>");
        }
        return  sb.toString();
    }

    private static String replaceQuote(List<String> list) {
        StringBuffer sb=new StringBuffer();
        if (list.size()>0) {
            sb.append("<blockquote>");
            for (String s : list) {
                Matcher quote_matcher=Pattern.compile(Markdown_Quote).matcher(s);
                if (quote_matcher.find()) {
                    sb.append(quote_matcher.group(2)).append("<br/>");
                }
            }
            sb.append("</blockquote>");
        }
        return sb.toString();
    }

    private static String replaceOrderedList(List<String> list) {
        StringBuilder ans=new StringBuilder();
        if(list.size()>0) {
            ans.append("<ul>");
            for (String line:list){
                Matcher ordered_list_matcher=Pattern.compile(Markdown_Ordered_List).matcher(line);
                if(ordered_list_matcher.find()) {
                    ans.append("<li>").append(ordered_list_matcher.group(2)).append("</li>");
                }
            }
            ans.append("</ul>");
        }
        return ans.toString();
    }

    private static String replaceUnorderedList(List<String> list) {
        StringBuilder ans=new StringBuilder();
        if (list.size()>0) {
            ans.append("<ul>");
            for (String line:list) {
                Matcher unordered_list_matcher=Pattern.compile(Markdown_Unordered_List).matcher(line);
                if (unordered_list_matcher.find()) {
                    ans.append("<li>").append(unordered_list_matcher.group(2)).append("</li>");
                }
            }
            ans.append("</ul>");
        }
        return ans.toString();
    }

    private static String replaceSeparateLine(String line) {
        Matcher separate_matcher=Pattern.compile(Markdown_Separate_Line).matcher(line);
        if (separate_matcher.find()) {
            line=separate_matcher.replaceAll("<hr/>");
        }
        return line;
    }

    private static String replaceImageLink(String line) {
        Matcher image_link_matcher=Pattern.compile(Markdown_IMAGE_LINK).matcher(line);
        if (image_link_matcher.find()) {
            line=image_link_matcher.replaceAll("<img src='$2' title='$1' alt='$1' />");
        }
        return line;
    }



    private static String replaceTitle(String line) {
        Matcher title_matcher=Pattern.compile(Markdown_Title).matcher(line);
        if (title_matcher.find()) {
            String star=title_matcher.group(1);
            line=title_matcher.replaceAll("<h"+star.length()+">$2</h"+star.length()+">");
        }
        return line;
    }

    private static String replaceSingleLineCode(String line) {
        Matcher single_line_code_matcher=Pattern.compile(Markdown_Single_Line_Code).matcher(line);
        if (single_line_code_matcher.find()) {
            line=single_line_code_matcher.replaceAll("<code>$2</code>");
        }
        return line;
    }

    private static String replaceLink(String line) {
        Matcher link_matcher=Pattern.compile(Markdown_Link).matcher(line);
        if (link_matcher.find()) {
            line=link_matcher.replaceAll("<a href='$2'>$1</a>");
        }
        return line;
    }

    private static String replaceDeleteLine(String line) {
        Matcher delete_matcher=Pattern.compile(Markdown_Delete_Line).matcher(line);
        if (delete_matcher.find()) {
            line=delete_matcher.replaceAll("<del>$2</del>");
        }
        return line;
    }

    private static String replaceItalic(String line) {
        Matcher italic_matcher=Pattern.compile(Markdown_Italic).matcher(line);
        if (italic_matcher.find()) {
            line=italic_matcher.replaceAll("<i>$2</i>");
        }
        return line;
    }

    private static String replaceBold(String line) {
        Matcher bold_matcher=Pattern.compile(Markdown_Bold).matcher(line);
        if (bold_matcher.find()) {
            //引用第二个捕获组的内容，转换为粗体
            line = bold_matcher.replaceAll("<b>$2</b>");
        }
        return line;
    }

    private static List<String> readMd(String mdFilepath) {
        FileReader fr = null;
        List<String>lines=new ArrayList<>();
        String line=null;
        BufferedReader br=null;

        try{
            fr=new FileReader(mdFilepath);
            br=new BufferedReader(fr);
            while((line =br.readLine()) !=null){
                if (line.trim().length()>0){
                    lines.add(line);
                }
            }
        }catch(IOException e){
            e.printStackTrace();
        }finally {
            try{
                if(fr!=null){
                    fr.close();
                }
                if(br!=null){
                    br.close();
                }
            }catch(IOException e){
                e.printStackTrace();
            }
        }
        return lines;
    }

}
