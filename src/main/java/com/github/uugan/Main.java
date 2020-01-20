package com.github.uugan;

import onbon.bx06.Bx6GEnv;
import onbon.bx06.Bx6GScreenClient;
import onbon.bx06.Bx6GScreenProfile;
import onbon.bx06.area.DynamicBxArea;
import onbon.bx06.area.TextCaptionBxArea;
import onbon.bx06.area.page.TextBxPage;
import onbon.bx06.cmd.dyn.DynamicBxAreaRule;
import onbon.bx06.file.ProgramBxFile;
import onbon.bx06.series.Bx6M;
import onbon.bx06.utils.DisplayStyleFactory;
import onbon.bx06.utils.TextBinary;
import org.apache.log4j.spi.LoggerFactory;
import org.json.JSONObject;
import org.rapidoid.config.Conf;
import org.rapidoid.setup.App;
import org.rapidoid.setup.On;


import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main implements Svc {
    private static Logger logger = Logger.getLogger(Main.class.getName());
    public static Main INSTANCE;
    private int _serverPort = 8080;
    private String _ledIp = "192.168.88.199";
    private int _ledPort = 5005;
    private Bx6GScreenClient _screen;
    private int _posX;
    private int _posY;

    public static void main(String[] args) {
        INSTANCE = new Main();
        try {
            INSTANCE.init();
            INSTANCE.start();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Exception occur", ex);
        }
    }

    @Override
    public void init() throws Exception {
        /*Initializing led*/
        try {
            Bx6GEnv.initial("log.properties", 15000);
            _ledIp = System.getProperty("led.ip");
            _ledPort = Integer.parseInt(System.getProperty("led.port"));
            _screen = new Bx6GScreenClient("MyScreen", new Bx6M());
            _posX = 90;
            _posY = 4;
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Exception occur", ex);
        }
        /*Initializing server*/
        _serverPort = Integer.parseInt(System.getProperty("server.port"));
        App.profiles("production");
        Conf.ON.set("port", _serverPort);
        Conf.HTTP.set("timeout", 300000);
        Conf.HTTP.set("serverName", "LedServer");
        App.boot();
        On.post("/").serve(new MainReqHandler());
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void stop() throws Exception {

    }

    private void initTCP() throws IOException {
        try {
            int cnt = 0;
            logger.info("Led connection:" + _screen.isConnected());
            while (!_screen.isConnected()) {
                if (cnt++ > 3) {
                    break;
                }
                if (!_screen.connect(_ledIp, _ledPort)) {
                    logger.log(Level.SEVERE, "Exception occur", "connect failed attempt:" + cnt);

                    Thread.sleep(3000);
                } else {
                    break;
                }
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Exception occur", ex);

        }
    }

    private String showText(String txt) throws Exception {
        initTCP();
        if (_screen != null && _screen.isConnected()) {
            Bx6GScreenProfile profile = _screen.getProfile();
            logger.info("screen width : " + profile.getWidth());
            logger.info("screen height : " + profile.getHeight());
            // Create Program file
            ProgramBxFile p000 = new ProgramBxFile(0, profile);
            // set the program frame show or not
            p000.setFrameShow(true);
            // frame move speed
            p000.setFrameSpeed(20);
            // Create a Text Area
            // (x, y, w, h)
            TextCaptionBxArea tArea = new TextCaptionBxArea(_posX, _posY, 32, 32, _screen.getProfile());
            // enable the area frame
            tArea.setFrameShow(true);
            // select which frame
            tArea.loadFrameImage(3);

            DisplayStyleFactory.DisplayStyle[] styles = DisplayStyleFactory.getStyles().toArray(new DisplayStyleFactory.DisplayStyle[0]);
            for (DisplayStyleFactory.DisplayStyle style : styles) {
                System.out.println(style);
            }


            // create a page, and add some text to the page
            TextBxPage page = new TextBxPage(txt);
            // whether to process text automatically
            // Set text horizontal alignment
            page.setHorizontalAlignment(TextBinary.Alignment.NEAR);
            // Set the text to be vertically centered
            page.setVerticalAlignment(TextBinary.Alignment.CENTER);
            // Set text font
            page.setFont(new Font("consolas", Font.PLAIN, 14));
            // Set text color
            page.setForeground(Color.red);
            // Set the area background color, the default is black
            page.setBackground(Color.darkGray);
            // Adjust the stunt mode
            page.setDisplayStyle(styles[4]);
            // adjust stunt speed
            page.setSpeed(1);
            // Adjust the dwell time in 10ms
            page.setStayTime(0);
            page.setHeadTailInterval(-2);

            // add the previously created page to area
            tArea.addPage(page);
            // add area to the show
            p000.addArea(tArea);

            if (p000.validate() != null) {
                System.out.println("P000 out of range");
                return "P000 out of range";
            }
            ArrayList<ProgramBxFile> plist = new ArrayList<ProgramBxFile>();
            plist.add(p000);

            _screen.deletePrograms();
            _screen.deleteAllDynamic();
            // Send the program file to the controller
            // There are three ways to send the program
            // You can choose according to your needs
            //
            // 1. writeProgramsAsync-asynchronous method, that is, the SDK will start its own thread to send
            // You need to pass in BxFileWriterListener
            // Can handle the corresponding event in the corresponding interface
            //screen.writeProgramsAsync(plist, new WriteProgramTextCaptionWithStyle ());

            //
            // 2. writePrograms-synchronous mode, that is, the SDK will block until the program is sent
            //
            // 将节目文件发送到控制器
            // 发送节目有三种方式
            // 可以根据自己的需求进行选择

            //
            // 1. writeProgramsAsync - 异步方式，即SDK会自己起线程来发送
            // 此时需传入 BxFileWriterListener
            // 可在相应的接口对相应的事件进行处理
            //screen.writeProgramsAsync(plist, new WriteProgramTextCaptionWithStyle());

            //
            // 2. writePrograms - 同步方式，即SDK会BLOCK住一直等到节目发送完毕
            _screen.writePrograms(plist);
            //
            // This method is usually not used
            // 3. Write the program to the controller in a synchronous manner. This method does not check anything, thus improving the transmission efficiency
            //screen.writeProgramQuickly(pf);
            _screen.disconnect();

            return "000";
        } else {
            logger.log(Level.SEVERE, "Exception occur", "error occurred: led device is not available.");
            return "led device is not available.";
        }
    }

    private String showDynamicTxt(String txt) throws Exception {
        initTCP();
        if (_screen != null && _screen.isConnected()) {
            Bx6GScreenProfile profile = _screen.getProfile();
            logger.info("screen width : " + profile.getWidth());
            logger.info("screen height : " + profile.getHeight());
            DynamicBxAreaRule dRule = new DynamicBxAreaRule();
            dRule.setId(0);
            dRule.setImmediatePlay((byte) 2);
            dRule.setRunMode((byte) 4);
            dRule.addRelativeProgram(1);

            DisplayStyleFactory.DisplayStyle[] styles = DisplayStyleFactory.getStyles().toArray(new DisplayStyleFactory.DisplayStyle[0]);
            for (DisplayStyleFactory.DisplayStyle style : styles) {
                System.out.println(style);
            }

            DynamicBxArea dArea = new DynamicBxArea(_posX, _posY, 32, 32, profile);
            TextBxPage page = new TextBxPage(txt);
            page.setDisplayStyle(styles[4]);
            page.setHeadTailInterval(-2);
            dArea.addPage(page);

            _screen.deletePrograms();
            _screen.deleteAllDynamic();

            _screen.writeDynamic(dRule, dArea);

            Thread.sleep(5000); //TODO: check!
            _screen.disconnect();
            return "000";
        } else {
            logger.log(Level.SEVERE, "Exception occur", "error occurred: led device is not available.");
            return "error occurred: led device is not available.";
        }
    }

    public String onData(String uri, JSONObject json) {
        String type = json.getString("type");
        JSONObject joRet = new JSONObject();
        switch (type) {
            case "text": {
                String txt = json.getString("txt");
                try {
                    String ret = showText(txt);
                    if (ret.equals("000")) {
                        joRet.put("code", "000");
                        joRet.put("msg", "Success");
                    } else {
                        joRet.put("code", "001");
                        joRet.put("msg", ret);
                    }
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, ex.getMessage(), ex);
                }

                break;
            }
            case "dynamictext": {
                String txt = json.getString("txt");
                try {
                    String ret = showDynamicTxt(txt);
                    if (ret.equals("000")) {
                        joRet.put("code", "000");
                        joRet.put("msg", "Success");
                    } else {
                        joRet.put("code", "001");
                        joRet.put("msg", ret);
                    }
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, ex.getMessage(), ex);
                }
                break;
            }
            case "show_number": {
                break;
            }
            case "check": {
                break;
            }
            default: {
                break;
            }

        }
        return joRet.toString();
    }
}
