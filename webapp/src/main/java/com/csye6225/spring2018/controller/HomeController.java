package com.csye6225.spring2018.controller;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.csye6225.spring2018.pojo.User;
import com.csye6225.spring2018.repositories.UserRepository;
import com.csye6225.spring2018.service.UserService;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Controller
public class HomeController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    private UserService userService;

    @Autowired
    public HomeController(UserService userService) {
        this.userService = userService;
    }

    private final static Logger logger = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    private Environment environment;

    @Value("${spring.profiles.active}")
    private String activeProfile;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index() {
        logger.info("Loading welcome page.");
        return "index";
    }

    @RequestMapping(value = "/loginHome", method = RequestMethod.POST)
    public String home(HttpServletRequest request, HttpSession session, Model mav) throws IOException, InterruptedException {

        System.out.println("Current Profile = " + activeProfile);
        String userName = request.getParameter("username");
        String password = request.getParameter("password");

        session = request.getSession();
        System.out.println("username =====" + userName + " password ====" + password);

        if (userName.isEmpty() || password.isEmpty()) {
            mav.addAttribute("home", "Please enter credentials");
            return "index";
        } else {
            User user = userService.findByUserName(userName);
            if (user == null) {
                mav.addAttribute("home", "Please enter a valid username. Username is your Email Id.");
                return "index";
            } else {
                String pass = user.getPassword();
                if (bCryptPasswordEncoder.matches(password, pass)) {

                    mav.addAttribute("home", "You are Logged In. Current Time is : " + new Date().toString());
                    mav.addAttribute("user", user);
                    mav.addAttribute("activeProfile",activeProfile);

                    logger.info("Loading home page.");
                    return "home";
                } else {
                    mav.addAttribute("home", "It seems you have entered a wrong password.");
                    return "index";
                }
            }
        }
    }

    @RequestMapping(value = "/user/logout", method = RequestMethod.POST)
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        session.invalidate();
        session = null;
        return "index";
    }

    @RequestMapping(value = "/user/signup", method = RequestMethod.GET)
    public String registerUserPage() {
        logger.info("Loading Signup Page.");
        return "registeruser";
    }

    @RequestMapping(value = "/user/register", method = RequestMethod.POST)
    public String save(HttpServletRequest request, Model model) {
        Pattern pattern;
        Matcher matcher;
        String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        pattern = Pattern.compile(EMAIL_PATTERN);

        String userName = request.getParameter("username");
        String password = request.getParameter("password");
        String aboutMe = request.getParameter("aboutMe");

        matcher = pattern.matcher(userName);
        if (matcher.matches()) {

            System.out.println("Username is --------" + userName);
            System.out.println("Password is ---------" + password);

            password = bCryptPasswordEncoder.encode(password);
            try {
                User userExists = userService.findByUserName(userName);
                if (userExists == null) {
                    User user = new User();
                    user.setUserName(userName);
                    user.setPassword(password);
                    user.setAboutMe(aboutMe);
                    user.setName("N");
                    userRepository.save(user);
                    model.addAttribute("success", "User account successfully created! Please login to continue");
                    return "index";
                } else {
                    model.addAttribute("error", "User already exists.");
                    return "registeruser";
                }
            } catch (DataIntegrityViolationException e) {
                model.addAttribute("error", "Something went wrong. Please try after sometime.");
                return "registeruser";
            }
        } else {
            model.addAttribute("error", "Please enter a valid email id as User Name.");
            return "registeruser";
        }
    }
    private static String bucketName = "web-app-csye6225-spring2018-singhpras.me";
    @RequestMapping(value = "/readImage", method = RequestMethod.POST)
    public String writeImage(HttpServletRequest request, HttpSession session, @RequestParam("file") MultipartFile multipartFile, Model mav)
            throws DataAccessException, IOException {


        String userName = request.getParameter("userName");
        System.out.println("Image is being saved for======" + userName);
        User user = userService.findByUserName(userName);

        try {
            if (multipartFile != null) {
                BufferedImage image = null;

                File myFile = new File(multipartFile.getOriginalFilename());
                System.out.println("Full File Name and Path =====" + myFile.getAbsolutePath());
                String absPath = request.getParameter("file");
                System.out.println("Full File Name and Path =====" + absPath);
                String fileName = myFile.getName();
                System.out.println("Name of the file ======= " + fileName);


                if (activeProfile.equalsIgnoreCase("dev")) {
                    myFile.createNewFile();
                    FileOutputStream fos = new FileOutputStream(myFile);
                    fos.write(multipartFile.getBytes());
                    fos.close();
                    image = ImageIO.read(myFile);
                    String path = "/home/pvs/csye6225/dev/csye6225-spring2018/webapp/src/main/resources/static/images/" + user.getUserName();
                    ImageIO.write(image, "jpg", new File(path));
                    user.setName("Y");
                    userRepository.save(user);


                }
                else {

                    String fileName1 = multipartFile.getOriginalFilename();
                    String filePath = request.getServletContext().getRealPath("/image");

                    File targetFilePath = new File(filePath);
                    File targetFile = new File(targetFilePath, fileName1);
                    String srcFilePath = filePath.replace("ROOT", "web-app");
                    File dirUpdated = new File(srcFilePath);
                    File srcImageFile = new File(new File(srcFilePath), fileName1);

                    if (!dirUpdated.exists()) {
                        dirUpdated.mkdirs();
                    }

                    if (!targetFilePath.exists()) {
                        targetFilePath.mkdirs();
                    }

                    String picUrl = "";
                    picUrl = "https://s3.amazonaws.com/" + bucketName + "/" + fileName1;
                    multipartFile.transferTo(srcImageFile);
                    FileUtils.copyFile(srcImageFile, targetFile);
                    session.setAttribute("image",picUrl);


                    AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                            .withCredentials(new InstanceProfileCredentialsProvider(false)).build();

                    //AmazonS3 s3Client = new AmazonS3Client();


                    try {
                        System.out.println("Uploading a new object to S3 from a file\n");
                        File file = new File(targetFile.toString());
                        s3Client.putObject(new PutObjectRequest(
                                bucketName, String.valueOf(user.getUserId()), file));
                        s3Client.setObjectAcl(bucketName,String.valueOf(user.getUserId()), CannedAccessControlList.PublicRead);
                        logger.info("FileName for upload is "+fileName1);
                        user.setName("Y");
                        userRepository.save(user);
                        //mav.addAttribute("activeProfile","aws");

                    } catch (AmazonServiceException ase) {

                        System.out.println("Caught an AmazonServiceException, which " +
                                "means your request made it " +
                                "to Amazon S3, but was rejected with an error response" +
                                " for some reason.");
                        System.out.println("Error Message:    " + ase.getMessage());
                        System.out.println("HTTP Status Code: " + ase.getStatusCode());
                        System.out.println("AWS Error Code:   " + ase.getErrorCode());
                        System.out.println("Error Type:       " + ase.getErrorType());
                        System.out.println("Request ID:       " + ase.getRequestId());
                    } catch (AmazonClientException ace) {
                        System.out.println("Caught an AmazonClientException, which " +
                                "means the client encountered " +
                                "an internal error while trying to " +
                                "communicate with S3, " +
                                "such as not being able to access the network.");
                        System.out.println("Error Message: " + ace.getMessage());
                    }


                }


            }
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
        return "index";
    }

    @RequestMapping(value = "/user/updateAboutMe", method = RequestMethod.POST)
    public String updateAboutMe(HttpServletRequest request, HttpSession session) {

        String userName = request.getParameter("userName");
        String aboutMe = request.getParameter("aboutMe");
        System.out.println("about me changed  ---" + aboutMe);
        User user = userService.findByUserName(userName);
        user.setAboutMe(aboutMe);
        userRepository.save(user);
        return "index";
    }

    @RequestMapping(value = "/user/search", method = RequestMethod.POST)
    public String searchUser(HttpServletRequest request, HttpSession session, Model mav) {
        logger.info("Loading Info Page.");
        String userName = request.getParameter("emailId");
        User user = userService.findByUserName(userName);
        if (user == null) {
            mav.addAttribute("user", "No Results Found. User does not exist");
            return "index";
        } else {
            mav.addAttribute("user", user);
            return "searchResult";
        }
    }


    @RequestMapping(value = "/deleteImage", method = RequestMethod.POST)
    public String deleteImageS3(HttpServletRequest request, HttpSession session) {

        ////////////////*********LOCAL FILE DELETION ****************////////////////////////////

        String userName = request.getParameter("userName");
        User user = userService.findByUserName(userName);

        if (activeProfile.equalsIgnoreCase("dev")) {

            try {

                File file = new File("/home/pvs/csye6225/dev/csye6225-spring2018/webapp/src/main/resources/static/images/" + user.getUserName());

                if (file.delete()) {
                    System.out.println(file.getName() + " is deleted!");
                    user.setName("N");
                    userRepository.save(user);
                    return "index";
                } else {
                    System.out.println("Delete operation is failed.");
                    return "error";
                }

            } catch (Exception e) {

                e.printStackTrace();
                return "error";

            }
        }


        ////////******************* S3 DELETION ************************//////////

        else {

            String userId = request.getParameter("userId");
            AmazonS3 s3Client = new AmazonS3Client();
            try {

                System.out.println("Deleting an object to S3 from a file\n");
                s3Client.deleteObject(bucketName, userId);
                user.setName("N");
                userRepository.save(user);


            } catch (AmazonServiceException ase) {
                System.out.println("Caught  AmazonServiceException, which " +
                        "means your request made it " +
                        "to Amazon S3, but was rejected with an error response" +
                        " for some reason.");
                System.out.println("Error Message:    " + ase.getMessage());
                System.out.println("HTTP Status Code: " + ase.getStatusCode());
                System.out.println("AWS Error Code:   " + ase.getErrorCode());
                System.out.println("Error Type:       " + ase.getErrorType());
                System.out.println("Request ID:       " + ase.getRequestId());

            } catch (AmazonClientException ace) {

                System.out.println("Caught an AmazonClientException, which " +
                        "means the client encountered " +
                        "an internal error while trying to " +
                        "communicate with S3, " +
                        "such as not being able to access the network.");
                System.out.println("Error Message: " + ace.getMessage());
            }
            return "index";
        }
    }

    //// Reset Password

    @RequestMapping(value = "/resetPassword", method = RequestMethod.POST)
    public String loadResetPasswordPage(){

        return "resetPassword";

    }

    @RequestMapping(value = "/sendResetEmail", method = RequestMethod.POST)
    public String passwordReset(HttpSession session, HttpServletRequest request,
                                HttpServletResponse response){
        String userName = request.getParameter("resetEmail");
        System.out.println("Send reset link to: "+userName);

/////////////////////////////// BEGIN ////////////////////////////////////


        AmazonSNSClient snsClient = new AmazonSNSClient(new InstanceProfileCredentialsProvider());
        snsClient.setRegion(Region.getRegion(Regions.US_EAST_1));

        String topicArn = snsClient.createTopic("resetpassword").getTopicArn();

        PublishRequest publishRequest = new PublishRequest(topicArn, userName);
        PublishResult publishResult = snsClient.publish(publishRequest);
        response.setStatus(HttpServletResponse.SC_OK);





////////////////////////////////////// END //////////////////////////////////



        return "resetConfirmation";
    }
}
































