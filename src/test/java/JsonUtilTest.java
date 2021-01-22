import com.dn.utils.JsonUtils;

import java.util.*;

public class JsonUtilTest {
    private static String json = "[{\"id\":\"4a9a99e8-ddf2-415b-aa46-b52ab4f2e409\",\"name\":\"语文\",\"studentList\":[{\"id\":1,\"name\":\"小明\"},{\"id\":2,\"name\":\"小红\"}]},{\"id\":\"ffef179a-565f-4c19-917a-8a3b08f1f56d\",\"name\":\"数学\",\"studentList\":[{\"id\":3,\"name\":\"朱元璋\"},{\"id\":4,\"name\":\"李善长\"}]}]";

    private static String mapJson = "{\"data\":[{\"id\":\"a9567059-181d-4157-9139-dba3f7cfdd0c\",\"name\":\"语文\",\"studentList\":[{\"id\":1,\"name\":\"小明\"},{\"id\":2,\"name\":\"小红\"}]},{\"id\":\"4720cf67-7636-45f7-9100-296ac8715ee3\",\"name\":\"数学\",\"studentList\":[{\"id\":3,\"name\":\"朱元璋\"},{\"id\":4,\"name\":\"李善长\"}]}],\"message\":\"请求成功\",\"status\":200}";

    public static void beanToJson() {
        Student student1 = new Student(1, "小明");
        Student student2 = new Student(2, "小红");
        Course course1 = new Course();
        course1.setId(UUID.randomUUID().toString());
        course1.setName("语文");
        course1.setStudentList(Arrays.asList(student1, student2));

        Student student3 = new Student(3, "朱元璋");
        Student student4 = new Student(4, "李善长");
        Course course2 = new Course();
        course2.setId(UUID.randomUUID().toString());
        course2.setName("数学");
        course2.setStudentList(Arrays.asList(student3, student4));

        List<Course> courseList = new ArrayList();
        courseList.add(course1);
        courseList.add(course2);
        String json = JsonUtils.toJson(courseList);
        System.out.println(json);
    }

    public static void jsonToList() {
        List<Course> courses = JsonUtils.toList(json, Course.class);
        System.out.println(courses);
    }

    @Deprecated
    public static void mapToJson() {
        Student student1 = new Student(1, "小明");
        Student student2 = new Student(2, "小红");
        Course course1 = new Course();
        course1.setId(UUID.randomUUID().toString());
        course1.setName("语文");
        course1.setStudentList(Arrays.asList(student1, student2));

        Student student3 = new Student(3, "朱元璋");
        Student student4 = new Student(4, "李善长");
        Course course2 = new Course();
        course2.setId(UUID.randomUUID().toString());
        course2.setName("数学");
        course2.setStudentList(Arrays.asList(student3, student4));

        List<Course> courseList = new ArrayList();
        courseList.add(course1);
        courseList.add(course2);

        Map<String, Object> map = new HashMap();
        map.put("status", 200);
        map.put("message", "请求成功");
        map.put("data", courseList);

        String s = JsonUtils.toJson(map);
        System.out.println(s);
    }


    public static void main(String[] args) {

    }
}
