import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SomeTask {
	private static void findChar() throws IOException {
		Path path = Paths.get("C:/Users/user/Desktop/word.txt");
		byte[] bytes = Files.readAllBytes(path);
		String content = new String(bytes, "UTF-8");
		List<String> chars = new ArrayList<String>();
		for (int i = 0; i < content.length(); i++) {
			String a = content.substring(i, i + 1);
			if (!chars.contains(a)) {
				chars.add(a);
			}

		}
		Collections.sort(chars);
		StringBuilder b = new StringBuilder();
		for (String str : chars) {
			b.append(str);
		}
		Files.write(Paths.get("C:/Users/user/Desktop/word2.txt"), b.toString()
				.getBytes("UTF-8"));
	}

	private static void changeGBK2UTF() throws IOException {
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

	public static void main(String[] args) throws IOException {
		findChar();
	}
}
