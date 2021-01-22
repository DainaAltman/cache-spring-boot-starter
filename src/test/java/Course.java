import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class Course implements Serializable {
    private String id;
    private String name;
    private List<Student> studentList = new ArrayList();
}
