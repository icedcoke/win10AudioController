
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import javax.swing.JPanel;

public class Audio {

    // 全局的位置变量，用于表示鼠标在窗口上的位置
    static Point origin = new Point();
    static int afWidth = 90, afHeight = 248, afTop = 0, screenHeight = 0, barTop = 44;
    static boolean selectedFrame = false, round = false, onShow = true, reduce = false;
    static int soundValue = 20;
    static int interval = 30;

    static ImageIcon image ;
    static TrayIcon trayIcon;

    public static void main(String args[]) {

        JFrame audioFrame = new JFrame();
        audioFrame.setSize(afWidth, afHeight);
        audioFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        audioFrame.setUndecorated(true);//窗口去边框
        audioFrame.setBackground(new Color(0, 0, 0, 0));//设置窗口为透明色
        audioFrame.setType(JFrame.Type.UTILITY);

        Toolkit kit = Toolkit.getDefaultToolkit(); //定义工具包
        Dimension screenSize = kit.getScreenSize(); //获取屏幕的尺寸
        Insets screenInerts = kit.getScreenInsets(audioFrame.getGraphicsConfiguration());   //获取屏幕边界信息

        PopupMenu popupMenu = new PopupMenu();

        //创建弹出菜单中的退出项
        MenuItem itemExit = new MenuItem("Exit");
        itemExit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        popupMenu.add(itemExit);
        audioFrame.add(popupMenu);

        image = new ImageIcon("Audio/src/sound.png");
        trayIcon = new TrayIcon(image.getImage(),"音量控制器", popupMenu);
        final SystemTray systemTray = SystemTray.getSystemTray();
        trayIcon.setImageAutoSize(true);
        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {

                if(audioFrame.getExtendedState()==1){
                    audioFrame.setExtendedState(JFrame.NORMAL);
                }else{
                    audioFrame.setExtendedState(JFrame.HIDE_ON_CLOSE);
                }
            }
        });

        if (!SystemTray.isSupported()) {
            System.out.println("系统不支持托盘");
            return;
        }

        try {
            SystemTray.getSystemTray().add(trayIcon);
        } catch (AWTException e1) {
            e1.printStackTrace();
        }

        int screenWidth = screenSize.width; //获取屏幕的宽
        screenHeight = screenSize.height; //获取屏幕的高
        afTop = screenHeight - screenInerts.bottom - afHeight;

        audioFrame.setLocation(screenWidth - afWidth - 100, screenHeight); //设置窗口的位置

        ImageIcon pumpIcon = new ImageIcon("Audio/src/pump.png");//指定图片对象
        ImageIcon handleIcon = new ImageIcon("Audio/src/handle.png");//指定图片对象
        pumpIcon.setImage(pumpIcon.getImage().getScaledInstance(55, 150,
                Image.SCALE_DEFAULT));//设置图片的尺寸
        handleIcon.setImage(handleIcon.getImage().getScaledInstance(90, 100,
                Image.SCALE_DEFAULT));//设置图片的尺寸

        JLabel pumpLabel = new JLabel(pumpIcon);//将背景图放在标签里。
        JLabel handleLabel = new JLabel(handleIcon);//将背景图放在标签里。
        JLabel soundLabel = new JLabel(Integer.toString(soundValue), JLabel.CENTER); //音量大小
        JPanel barBox = new JPanel();
        JPanel bar = new JPanel();

        handleLabel.setBounds(0, 70, handleIcon.getIconWidth(), handleIcon.getIconHeight());//设置背景标签的位置
        pumpLabel.setBounds(15, 0, pumpIcon.getIconWidth(), pumpIcon.getIconHeight());//设置背景标签的位置
        soundLabel.setBounds(-8, -22, 100, 100);
        barBox.setBounds(34, barTop, 16, 80);
        bar.setBounds(34, barTop + 60, 16, 20);

        Font font = new Font("宋体", Font.BOLD, 18);
        soundLabel.setFont(font);
        soundLabel.setForeground(Color.white);
        barBox.setBackground(new Color(149, 87, 60, 75));
        bar.setBackground(new Color(10, 255, 33));

        JLayeredPane layer = new JLayeredPane();        //分层面板
        layer.setBounds(0, 98, afWidth, pumpIcon.getIconHeight());//设置背景标签的位置
        layer.add(pumpLabel, new Integer(0));
        layer.add(soundLabel, new Integer(1));
        layer.add(barBox, new Integer(2));
        layer.add(bar, new Integer(3));

        audioFrame.getLayeredPane().add(layer);
        audioFrame.getLayeredPane().add(handleLabel);

        Container cp = audioFrame.getContentPane();
        cp.setLayout(new BorderLayout());
        ((JPanel) cp).setOpaque(false);
        audioFrame.setVisible(true);
//        System.out.println(System.getProperty("user.dir"));

        ExecutorService executor = Executors.newFixedThreadPool(2);

//        future.thenAccept(e -> System.out.println(e));

        audioFrame.addMouseListener(new MouseAdapter() {
            // 按下（mousePressed 不是点击，而是鼠标被按下没有抬起）
            public void mousePressed(MouseEvent e) {
                // 当鼠标按下的时候获得窗口当前的位置
                origin.x = e.getX();
                origin.y = e.getY();

                if (origin.y > 98) {
                    selectedFrame = true;
                } else {
                    selectedFrame = false;
                }

                if(e.getButton() == 3){

                    popupMenu.show(e.getComponent(), audioFrame.getX() + e.getX(), audioFrame.getY() + e.getY());
                }
            }
        });
        audioFrame.addMouseMotionListener(new MouseMotionAdapter() {
            // 拖动（mouseDragged 指的不是鼠标在窗口中移动，而是用鼠标拖动）
            public void mouseDragged(MouseEvent e) {

                if (selectedFrame == true) {
                    // 当鼠标拖动时获取窗口当前位置
                    Point p = audioFrame.getLocation();
                    // 设置窗口的位置
                    // 窗口当前的位置 + 鼠标当前在窗口的位置 - 鼠标按下的时候在窗口的位置
                    audioFrame.setLocation(p.x + e.getX() - origin.x, p.y + e.getY() - origin.y);
                } else {
                    //设置手柄位置
                    Point p = handleLabel.getLocation();
                    int y = p.y + e.getY() - origin.y;
                    if (y < 0) {
                        y = 0;
                    } else if (y > 70) {
                        y = 70;
                    }
                    handleLabel.setLocation(p.x, y);
                    origin.y = e.getY();

                    //设置音量
                    if ((y < 10 && round) || (y > 60 && !round)) {

                        soundValue += 4;
                        round = !round;
                        soundValue = soundValue > 100 ? 100 : soundValue;
                        soundLabel.setText(Integer.toString(soundValue));

                        int barHeight = soundValue * 8 / 10;
                        bar.setBounds(bar.getX(), barTop + (80 - barHeight), bar.getWidth(), barHeight);

                        reduce = true;

                        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(new Supplier<Integer>() {

                            public Integer get() {
                                //异步增加音量
                                controlSystemVolume("1");
                                return 1;
                            }
                        }, executor);
//                        future.thenAccept(fe -> System.out.println(fe));
                    }

                }

            }
        });

        audioFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowDeiconified(WindowEvent e) {

                audioFrame.setLocation(audioFrame.getX(), screenHeight);
                onShow = true;
                audioFrame.setOpacity(1);
            }

            public void windowIconified(WindowEvent e) {

                audioFrame.setOpacity(0);
                reduce = false;
            }

            public void windowDeactivated(WindowEvent arg0) {

                audioFrame.setExtendedState(JFrame.HIDE_ON_CLOSE);
            }
        });

        //计时器
        Timer timer = new Timer(10, new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                if (soundValue > 0 && reduce) {

                    if (interval > 0) {
                        interval--;
                    } else {
                        interval = 30;

                        soundValue -= 2;
                        soundLabel.setText(Integer.toString(soundValue));

                        int barHeight = soundValue * 8 / 10;
                        bar.setBounds(bar.getX(), barTop + (80 - barHeight), bar.getWidth(), barHeight);

                        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(new Supplier<Integer>() {

                            public Integer get() {
                                //异步减音量
                                controlSystemVolume("0");
                                return 1;
                            }
                        }, executor);
                    }
                }

                if (onShow) {

                    int top = audioFrame.getY() - 10;
                    if (top > afTop) {
                        audioFrame.setLocation(audioFrame.getX(), top);
                    } else {
                        onShow = false;
                    }
                }
            }
        });

        timer.start();
    }

    public static void controlSystemVolume(String type) {
        try {
            if (type == null || "".equals(type.trim())) {
                System.out.println("type 参数为空,不进行操作...");
            }
            /**tempFile：vbs 文件
             * vbsMessage：vbs 文件的内容*/
            String vbsMessage = "";
            File tempFile = null;
            Runtime runtime = Runtime.getRuntime();
            switch (type) {
                case "0":
                    tempFile = new File("temp", "volumeDown.vbs");
                    vbsMessage = !tempFile.exists() ? "CreateObject(\"Wscript.Shell\").Sendkeys \"棶棶\"" : "";
                    break;
                case "1":
                    tempFile = new File("temp", "volumeAdd.vbs");
                    vbsMessage = !tempFile.exists() ? "CreateObject(\"Wscript.Shell\").Sendkeys \"棷棷棷棷\"" : "";
                    break;
                default:
                    return;
            }
            /**
             * 当2个vbs文件不存在时，则创建它们，应用默认编码为 utf-8 时，创建的 vbs 脚本运行时报错
             * 于是使用 OutputStreamWriter 将 vbs 文件编码改成gbd就正常了
             */
            if (!tempFile.exists() && !vbsMessage.equals("")) {
                if (!tempFile.getParentFile().exists()) {
                    tempFile.getParentFile().mkdirs();
                }
                tempFile.createNewFile();
                FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, "GBK");
                outputStreamWriter.write(vbsMessage);
                outputStreamWriter.flush();
                outputStreamWriter.close();
                System.out.println("vbs 文件不存在，新建成功：" + tempFile.getAbsolutePath());
            }
            runtime.exec("wscript " + tempFile.getAbsolutePath()).waitFor();
//            System.out.println("音量控制完成.");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
