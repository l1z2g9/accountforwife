
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class ChangeGBK2UTF {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		Path path = Paths
				.get("C:/Users/user/workspace/accountforwife/accountforwife/desktop/src");
		String pattern = "**/*.java";
		final PathMatcher matcher = FileSystems.getDefault().getPathMatcher(
				"glob:" + pattern);
		SimpleFileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file,
					BasicFileAttributes attrs) {
				if (matcher.matches(file)) {
					System.out.println("to process " + file);
					try {
						byte[] bytes = Files.readAllBytes(file);
						String gbkContent = new String(bytes, "gb18030");

						Files.write(file, gbkContent.getBytes("UTF-8"));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				return FileVisitResult.CONTINUE;
			}
		};
		Files.walkFileTree(path, visitor);
	}
}
