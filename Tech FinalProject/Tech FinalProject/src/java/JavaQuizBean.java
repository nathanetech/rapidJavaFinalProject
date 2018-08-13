/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author TechNathan
 */
@ManagedBean(name = "javaQuizBean")
@SessionScoped
public class JavaQuizBean implements Serializable {

    /**
     * Creates a new instance of JavaQuizBean
     */
    public String username;
    public String password;
    public String chapterNo;
    public String firstName;
    public String mi;
    public String lastName;
    public String email;
    public Boolean ShowLogin = true;
    public Boolean hasLoggedIn = false;
    public String label;
    public PreparedStatement ps;
    public Connection conn;
    public Chapter[] chapterList;
    public List<Question> questionList = new ArrayList<>();

    public JavaQuizBean() {

    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getChapterNo() {
        return chapterNo;
    }

    public void setChapterNo(String chapterNo) {
        this.chapterNo = chapterNo;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMi() {
        return mi;
    }

    public void setMi(String mi) {
        this.mi = mi;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getShowLogin() {
        return ShowLogin;
    }

    public void setShowLogin(Boolean ShowLogin) {
        this.ShowLogin = ShowLogin;
    }

    public Boolean getHasLoggedIn() {
        return hasLoggedIn;
    }

    public void setHasLoggedIn(Boolean HasLoggedIn) {
        this.hasLoggedIn = HasLoggedIn;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<Question> getQuestionList() {
        return questionList;
    }

    public void setQuestionList(List<Question> questionList) {
        this.questionList = questionList;
    }

    public PreparedStatement getpsmt() {
        return ps;
    }

    public void setpsmt(PreparedStatement psmt) {
        this.ps = psmt;
    }

    public String getconn() {
        return label;
    }

    public void setconn(Connection conn) {
        this.conn = conn;
    }

    public Chapter[] getFavChapterValue() {

        chapterList = new Chapter[44];

        for (int i = 0; i < 44; i++) {

            chapterList[i] = new Chapter("Chapter " + (i + 1),
                    Integer.toString(i + 1));
        }

        return chapterList;

    }

    public void checkAllQuestions() {
        questionList.forEach((q) -> {
            q.checkAnswer();
        });
    }

    public void changeChapter() {
        questionList.clear();
        try {
            ps = conn.prepareCall(
                    "select * from tech.intro10equiz where chapterNo =  ?");
            ps.setInt(1, Integer.parseInt(chapterNo));
            ResultSet rs = ps.executeQuery();

            if (!rs.isBeforeFirst()) {
                setLabel("Empty Result");
            }
            while (rs.next()) {
                questionList.add(new Question(rs.getInt("questionNo"),
                        rs.getString("question"), rs.getString("answerKey"), "",
                        rs.getString("choiceA"), rs.getString("choiceB"),
                        rs.getString("choiceC"), rs.getString("choiceD"),
                        rs.getString("choiceE"), rs.getString("hint")));
            }
            for (Question q : questionList) {
                ps = conn.prepareCall(
                        "select * from tech.intro10e where isCorrect = 1 and chapterNo =  ? and  questionNo =  ? and  username =  ? ORDER  BY time DESC LIMIT 1");
                ps.setInt(1, Integer.parseInt(chapterNo));
                ps.setInt(2, q.ID);
                ps.setString(3, username);
                rs = ps.executeQuery();

                if (rs.isBeforeFirst()) {
                    while (rs.next()) {
                        q.setIsAnswered(true);
                        q.setIsRight(rs.getBoolean("isCorrect"));
                        q.setLabelText("Correct");

                        StringBuilder s = new StringBuilder();
                        if (rs.getBoolean("answerA")) {
                            s.append("a");
                        }
                        if (rs.getBoolean("answerB")) {
                            s.append("b");
                        }
                        if (rs.getBoolean("answerC")) {
                            s.append("c");
                        }
                        if (rs.getBoolean("answerD")) {
                            s.append("d");
                        }
                        if (rs.getBoolean("answerE")) {
                            s.append("e");
                        }
                        if (s.toString().replaceAll("\\s+", "").length() == 1) {
                            q.setAnswer(s.toString().replaceAll("\\s+", ""));
                        } else {

                            q.setAnswerSet(s.toString().replaceAll("\\s+", "").split(""));
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            setLabel("You are not logged in");
        }
    }

    public void clear() {
        username = "";
        password = "";
        chapterNo = "";
        label = "";
        ShowLogin = true;
        hasLoggedIn = false;
        questionList.clear();
    }

    public void login() {
        try {

            // Load the JDBC driver
            Class.forName("com.mysql.jdbc.Driver");
            System.out.println("Driver loaded");
            // Establish a conn
            conn = DriverManager.getConnection(
                    "jdbc:mysql://liang.armstrong.edu:3306", "tech", "tiger");
            System.out.println("Database connected");

            ps = conn.prepareCall(
                    "select * from tech.user where username =  ? and  password =  ?");
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (username.replaceAll("\\s+", "").length() == 0
                    || password.replaceAll("\\s+", "").length() == 0) {
                setLabel("Please enter your username and password");
            } else if (!rs.isBeforeFirst()) {
                setLabel(
                        "Username not found, please enter a password and create an account");
            } else {
                setLabel("Login Successful");
                ps = conn.prepareCall(
                        "select * from tech.intro10equiz where chapterNo =  ?");
                ps.setInt(1, Integer.parseInt(chapterNo));
                rs = ps.executeQuery();

                if (!rs.isBeforeFirst()) {
                    setLabel("Empty Result");
                }
                while (rs.next()) {
                    questionList.add(new Question(rs.getInt("questionNo"),
                            rs.getString("question"), rs.getString("answerKey"), "",
                            rs.getString("choiceA"), rs.getString("choiceB"),
                            rs.getString("choiceC"), rs.getString("choiceD"),
                            rs.getString("choiceE"), rs.getString("hint")));
                }
                for (Question q : questionList) {
                    ps = conn.prepareCall(
                            "select * from tech.intro10e where isCorrect = 1 and chapterNo =  ? and  questionNo =  ? and username =  ? ORDER  BY time DESC LIMIT 1");
                    ps.setInt(1, Integer.parseInt(chapterNo));
                    ps.setInt(2, q.ID);
                    ps.setString(3, username);
                    rs = ps.executeQuery();

                    if (rs.isBeforeFirst()) {
                        while (rs.next()) {
                            q.setIsAnswered(true);
                            q.setIsRight(rs.getBoolean("isCorrect"));
                            q.setLabelText("Correct");

                            StringBuilder s = new StringBuilder();
                            if (rs.getBoolean("answerA")) {
                                s.append("a");
                            }
                            if (rs.getBoolean("answerB")) {
                                s.append("b");
                            }
                            if (rs.getBoolean("answerC")) {
                                s.append("c");
                            }
                            if (rs.getBoolean("answerD")) {
                                s.append("d");
                            }
                            if (rs.getBoolean("answerE")) {
                                s.append("e");
                            }
                            if (s.toString().replaceAll("\\s+",
                                    "").length() == 1) {

                                q.setAnswer(s.toString().replaceAll("\\s+", ""));
                            } else {

                                q.setAnswerSet(s.toString().replaceAll("\\s+", "").split(""));
                            }
                        }
                    }
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            setLabel("You are not logged in");
        }

        ShowLogin = false;
        hasLoggedIn = true;
    }

    public void createNewUser() {
        try {

            // Load the JDBC driver
            Class.forName("com.mysql.jdbc.Driver");
            System.out.println("Driver loaded");
            // Establish a conn
            conn = DriverManager.getConnection(
                    "jdbc:mysql://liang.armstrong.edu:3306", "tech", "tiger");
            System.out.println("Database connected");
            ps = conn.prepareStatement("select * from tech.user where username = ?");
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (username.replaceAll("\\s+", "").length() == 0) {
                setLabel("Please enter your username.");
                System.out.print("Please enter your username.");
            } else if (rs.isBeforeFirst()) {
                setLabel("Username already taken.");
                System.out.print("Username already taken.");
            } else {
                ps = conn.prepareStatement("insert into tech.user "
                        + "(username, password, firstname, lastname, mi) values (?, ?, ?, ?, ?)");
                ps.setString(1, username);
                ps.setString(2, password);
                ps.setString(3, firstName);
                ps.setString(4, lastName);
                ps.setString(5, mi);
                ps.executeUpdate();
                setLabel("Account created!");
                System.out.print("Account successfully created: " + username + " " + password);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            setLabel("You are not logged in");
        }
    }

    public class Chapter {

        public String cLbl;
        public String cValue;

        public Chapter(String cLbl, String cValue) {
            this.cLbl = cLbl;
            this.cValue = cValue;
        }

        public String getCLbl() {
            return cLbl;
        }

        public String getCValue() {
            return cValue;
        }

    }

    public class Question implements Serializable {

        public String QuestionText;
        public String Key;
        public String Answer = "";
        public String[] AnswerSet;
        public String A;
        public String B;
        public String C;
        public String D;
        public String E;
        public String Hint;
        public int ID;
        public boolean isRight;
        public boolean IsMultiChoice;
        public boolean IsAnswered;
        public boolean Cempty;
        public boolean Dempty;
        public boolean Eempty;
        public String LabelText;

        public Question() {
            this.QuestionText = "";
            this.Key = "";
            this.ID = 0;
            this.Answer = "";
            this.A = "";
            this.B = "";
            this.C = "";
            this.D = "";
            this.E = "";
            this.Hint = "";
            IsMultiChoice = false;
        }

        public Question(int id, String questionText, String key, String answer, String a, String b, String c, String d, String e, String hint) {
            this.QuestionText = questionText;
            this.Key = key;
            this.ID = id;
            this.Answer = answer;
            this.A = a;
            this.B = b;
            this.C = c;
            this.D = d;
            this.E = e;
            this.Hint = hint;
            if (Key.replaceAll("\\s+", "").length() > 1) {
                IsMultiChoice = true;
            } else {
                IsMultiChoice = false;
            }
            Eempty = E.length() != 0;
            Dempty = D.length() != 0;
            Cempty = C.length() != 0;
        }

        public String getQuestionText() {
            return QuestionText;
        }

        public String getKey() {
            return Key;
        }

        public String getAnswer() {
            return Answer;
        }

        public String[] getAnswerSet() {
            return AnswerSet;
        }

        public String getA() {
            return A;
        }

        public String getB() {
            return B;
        }

        public String getC() {
            return C;
        }

        public boolean getCempty() {
            return Cempty;
        }

        public String getD() {
            return D;
        }

        public boolean getDempty() {
            return Dempty;
        }

        public String getE() {
            return E;
        }

        public boolean getEempty() {
            return Eempty;
        }

        public int getID() {
            return ID;
        }

        public String getHint() {
            return Hint;
        }

        public boolean getIsRight() {
            return isRight;
        }

        public boolean getIsAnswered() {
            return IsAnswered;
        }

        public boolean getIsMultiChoice() {
            return IsMultiChoice;
        }

        public String getLabelText() {
            return LabelText;
        }

        public void setLabelText(String LabelText) {
            this.LabelText = LabelText;
        }

        public void setID(int id) {
            this.ID = id;
        }

        public void setE(String e) {
            this.E = e;
        }

        public void setEempty(boolean eempty) {
            this.Eempty = eempty;
        }

        public void setD(String d) {
            this.D = d;
        }

        public void setDempty(boolean dempty) {
            this.Dempty = dempty;
        }

        public void setC(String c) {
            this.C = c;
        }

        public void setCempty(boolean cempty) {
            this.Cempty = cempty;
        }

        public void setB(String b) {
            this.B = b;
        }

        public void setA(String a) {
            this.A = a;
        }

        public void setAnswer(String answer) {
            this.Answer = answer;
        }

        public void setAnswerSet(String[] answerSet) {
            this.AnswerSet = answerSet;
        }

        public void setKey(String key) {
            this.Key = key;
        }

        public void setHint(String hint) {
            this.Hint = hint;
        }

        public void setIsRight(boolean isRight) {
            this.isRight = isRight;
        }

        public void setIsAnswered(boolean isAnswered) {
            this.IsAnswered = isAnswered;
        }

        public void setIsMultiChoice(boolean isMultiChoice) {
            this.IsMultiChoice = isMultiChoice;
        }

        public void setQuestionText(String questionText) {
            this.QuestionText = questionText;
        }

        public void checkAnswer() {
            IsAnswered = true;
            // Answer = Key;
            if (AnswerSet == null && Answer == null) {
                Answer = "";
            } else if (Answer.replaceAll("\\s+", "").length() == 1) {

                if (Answer.replaceAll("\\s+", "").equals(Key.replaceAll("\\s+", ""))) {
                    isRight = true;
                    setLabelText("Correct");
                } else {
                    setLabelText("Wrong");
                }

            } else {
                StringBuilder answer = new StringBuilder();

                for (String s : AnswerSet) {
                    answer.append(s);
                }
                Answer = answer.toString();
                if (Answer.replaceAll("\\s+", "").equals(Key.replaceAll("\\s+", ""))) {
                    isRight = true;
                    setLabelText("Correct");
                } else {
                    isRight = false;
                    setLabelText("Wrong");
                }
            }

            try {
                HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
                String ipAddress = request.getHeader("X-FORWARDED-FOR");
                if (ipAddress == null) {
                    ipAddress = request.getRemoteAddr();
                }
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                Class.forName("com.mysql.jdbc.Driver");
                System.out.println("Driver loaded");
                // Establish a conn
                conn = DriverManager.getConnection(
                        "jdbc:mysql://liang.armstrong.edu:3306", "tech", "tiger");
                System.out.println("Database connected");
                ps = conn.prepareStatement("insert into tech.intro10e "
                        + "(chapterNo, questionNo, isCorrect, time, hostname, answerA, answerB, answerC, answerD, answerE, username) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                ps.setInt(1, Integer.parseInt(chapterNo));
                ps.setInt(2, ID);
                ps.setBoolean(3, isRight);
                ps.setTimestamp(4, timestamp);
                ps.setString(5, ipAddress);
                ps.setBoolean(6, Answer.contains("a"));
                ps.setBoolean(7, Answer.contains("b"));
                ps.setBoolean(8, Answer.contains("c"));
                ps.setBoolean(9, Answer.contains("d"));
                ps.setBoolean(10, Answer.contains("e"));
                ps.setString(11, username);
                ps.executeUpdate();
                System.out.print("Answer #" + ID + " Saved");
            } catch (Exception ex) {
                ex.printStackTrace();
                setLabel("Error");
            }
        }
    }

}
