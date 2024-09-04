public class MarkdownToHtmlTest {
    public static void main(String[] args) {
        String mdFilePath="src/README(MDN).md";
        String htmlFilePath="src/test-md-to-html.html";
        MarkdownToHtml.convert(mdFilePath,htmlFilePath);
    }
}
