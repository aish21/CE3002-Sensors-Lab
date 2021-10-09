package appinventor.ai_aish_akshu.HeartRateMonitor;

import android.os.Bundle;
import androidx.fragment.app.FragmentTransaction;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.runtime.AppInventorCompatActivity;
import com.google.appinventor.components.runtime.BluetoothClient;
import com.google.appinventor.components.runtime.Button;
import com.google.appinventor.components.runtime.Clock;
import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.EventDispatcher;
import com.google.appinventor.components.runtime.Form;
import com.google.appinventor.components.runtime.HandlesEventDispatching;
import com.google.appinventor.components.runtime.HorizontalArrangement;
import com.google.appinventor.components.runtime.Label;
import com.google.appinventor.components.runtime.ListPicker;
import com.google.appinventor.components.runtime.Notifier;
import com.google.appinventor.components.runtime.TextBox;
import com.google.appinventor.components.runtime.VerticalArrangement;
import com.google.appinventor.components.runtime.errors.PermissionException;
import com.google.appinventor.components.runtime.errors.StopBlocksExecution;
import com.google.appinventor.components.runtime.errors.YailRuntimeError;
import com.google.appinventor.components.runtime.util.RetValManager;
import com.google.appinventor.components.runtime.util.RuntimeErrorAlert;
import com.google.youngandroid.C0642runtime;
import com.sunny.SmtpClient.C0692SmtpClient;
import gnu.expr.Language;
import gnu.expr.ModuleBody;
import gnu.expr.ModuleInfo;
import gnu.expr.ModuleMethod;
import gnu.kawa.functions.Apply;
import gnu.kawa.functions.Format;
import gnu.kawa.functions.GetNamedPart;
import gnu.kawa.functions.IsEqual;
import gnu.kawa.reflect.Invoke;
import gnu.kawa.reflect.SlotGet;
import gnu.kawa.reflect.SlotSet;
import gnu.lists.Consumer;
import gnu.lists.FString;
import gnu.lists.LList;
import gnu.lists.Pair;
import gnu.lists.PairWithPosition;
import gnu.lists.VoidConsumer;
import gnu.mapping.CallContext;
import gnu.mapping.Environment;
import gnu.mapping.SimpleSymbol;
import gnu.mapping.Symbol;
import gnu.mapping.Values;
import gnu.mapping.WrongType;
import gnu.math.IntNum;
import kawa.lang.Promise;
import kawa.lib.C0654lists;
import kawa.lib.misc;
import kawa.lib.strings;
import kawa.standard.Scheme;
import kawa.standard.require;

/* compiled from: Screen1.yail */
public class Screen1 extends Form implements Runnable {
    static final SimpleSymbol Lit0 = ((SimpleSymbol) new SimpleSymbol("Screen1").readResolve());
    static final SimpleSymbol Lit1 = ((SimpleSymbol) new SimpleSymbol("getMessage").readResolve());
    static final SimpleSymbol Lit10;
    static final SimpleSymbol Lit100 = ((SimpleSymbol) new SimpleSymbol("resultLabel").readResolve());
    static final IntNum Lit101 = IntNum.make(22);
    static final FString Lit102 = new FString("com.google.appinventor.components.runtime.Label");
    static final FString Lit103 = new FString("com.google.appinventor.components.runtime.Label");
    static final SimpleSymbol Lit104 = ((SimpleSymbol) new SimpleSymbol("result").readResolve());
    static final SimpleSymbol Lit105 = ((SimpleSymbol) new SimpleSymbol("FontBold").readResolve());
    static final IntNum Lit106 = IntNum.make(30);
    static final SimpleSymbol Lit107 = ((SimpleSymbol) new SimpleSymbol("TextAlignment").readResolve());
    static final FString Lit108 = new FString("com.google.appinventor.components.runtime.Label");
    static final FString Lit109 = new FString("com.google.appinventor.components.runtime.BluetoothClient");
    static final SimpleSymbol Lit11 = ((SimpleSymbol) new SimpleSymbol("AlignVertical").readResolve());
    static final FString Lit110 = new FString("com.google.appinventor.components.runtime.BluetoothClient");
    static final FString Lit111 = new FString("com.google.appinventor.components.runtime.Clock");
    static final SimpleSymbol Lit112 = ((SimpleSymbol) new SimpleSymbol("Clock1").readResolve());
    static final FString Lit113 = new FString("com.google.appinventor.components.runtime.Clock");
    static final SimpleSymbol Lit114 = ((SimpleSymbol) new SimpleSymbol("BytesAvailableToReceive").readResolve());
    static final IntNum Lit115 = IntNum.make(0);
    static final PairWithPosition Lit116 = PairWithPosition.make(Lit10, PairWithPosition.make(Lit10, LList.Empty, "/tmp/1633789898217_0.8849879556365526-0/youngandroidproject/../src/appinventor/ai_aish_akshu/HeartRateMonitor/Screen1.yail", 905431), "/tmp/1633789898217_0.8849879556365526-0/youngandroidproject/../src/appinventor/ai_aish_akshu/HeartRateMonitor/Screen1.yail", 905423);
    static final SimpleSymbol Lit117 = ((SimpleSymbol) new SimpleSymbol("ReceiveText").readResolve());
    static final PairWithPosition Lit118 = PairWithPosition.make(Lit10, LList.Empty, "/tmp/1633789898217_0.8849879556365526-0/youngandroidproject/../src/appinventor/ai_aish_akshu/HeartRateMonitor/Screen1.yail", 905662);
    static final SimpleSymbol Lit119 = ((SimpleSymbol) new SimpleSymbol("SmtpClient1").readResolve());
    static final IntNum Lit12 = IntNum.make(2);
    static final SimpleSymbol Lit120 = ((SimpleSymbol) new SimpleSymbol("Send").readResolve());
    static final IntNum Lit121 = IntNum.make(465);
    static final PairWithPosition Lit122 = PairWithPosition.make((SimpleSymbol) new SimpleSymbol("any").readResolve(), LList.Empty, "/tmp/1633789898217_0.8849879556365526-0/youngandroidproject/../src/appinventor/ai_aish_akshu/HeartRateMonitor/Screen1.yail", 905909);
    static final PairWithPosition Lit123 = PairWithPosition.make(Lit14, PairWithPosition.make(Lit14, LList.Empty, "/tmp/1633789898217_0.8849879556365526-0/youngandroidproject/../src/appinventor/ai_aish_akshu/HeartRateMonitor/Screen1.yail", 906281), "/tmp/1633789898217_0.8849879556365526-0/youngandroidproject/../src/appinventor/ai_aish_akshu/HeartRateMonitor/Screen1.yail", 906275);
    static final PairWithPosition Lit124 = PairWithPosition.make(Lit14, PairWithPosition.make(Lit14, LList.Empty, "/tmp/1633789898217_0.8849879556365526-0/youngandroidproject/../src/appinventor/ai_aish_akshu/HeartRateMonitor/Screen1.yail", 906454), "/tmp/1633789898217_0.8849879556365526-0/youngandroidproject/../src/appinventor/ai_aish_akshu/HeartRateMonitor/Screen1.yail", 906448);
    static final PairWithPosition Lit125 = PairWithPosition.make(Lit14, PairWithPosition.make(Lit14, LList.Empty, "/tmp/1633789898217_0.8849879556365526-0/youngandroidproject/../src/appinventor/ai_aish_akshu/HeartRateMonitor/Screen1.yail", 906478), "/tmp/1633789898217_0.8849879556365526-0/youngandroidproject/../src/appinventor/ai_aish_akshu/HeartRateMonitor/Screen1.yail", 906472);
    static final PairWithPosition Lit126;
    static final SimpleSymbol Lit127 = ((SimpleSymbol) new SimpleSymbol("Clock1$Timer").readResolve());
    static final SimpleSymbol Lit128 = ((SimpleSymbol) new SimpleSymbol("Timer").readResolve());
    static final FString Lit129 = new FString("com.google.appinventor.components.runtime.Notifier");
    static final SimpleSymbol Lit13 = ((SimpleSymbol) new SimpleSymbol("AppName").readResolve());
    static final FString Lit130 = new FString("com.google.appinventor.components.runtime.Notifier");
    static final FString Lit131 = new FString("com.sunny.SmtpClient.SmtpClient");
    static final FString Lit132 = new FString("com.sunny.SmtpClient.SmtpClient");
    static final SimpleSymbol Lit133 = ((SimpleSymbol) new SimpleSymbol("get-simple-name").readResolve());
    static final SimpleSymbol Lit134 = ((SimpleSymbol) new SimpleSymbol("android-log-form").readResolve());
    static final SimpleSymbol Lit135 = ((SimpleSymbol) new SimpleSymbol("add-to-form-environment").readResolve());
    static final SimpleSymbol Lit136 = ((SimpleSymbol) new SimpleSymbol("lookup-in-form-environment").readResolve());
    static final SimpleSymbol Lit137 = ((SimpleSymbol) new SimpleSymbol("is-bound-in-form-environment").readResolve());
    static final SimpleSymbol Lit138 = ((SimpleSymbol) new SimpleSymbol("add-to-global-var-environment").readResolve());
    static final SimpleSymbol Lit139 = ((SimpleSymbol) new SimpleSymbol("add-to-events").readResolve());
    static final SimpleSymbol Lit14;
    static final SimpleSymbol Lit140 = ((SimpleSymbol) new SimpleSymbol("add-to-components").readResolve());
    static final SimpleSymbol Lit141 = ((SimpleSymbol) new SimpleSymbol("add-to-global-vars").readResolve());
    static final SimpleSymbol Lit142 = ((SimpleSymbol) new SimpleSymbol("add-to-form-do-after-creation").readResolve());
    static final SimpleSymbol Lit143 = ((SimpleSymbol) new SimpleSymbol("send-error").readResolve());
    static final SimpleSymbol Lit144 = ((SimpleSymbol) new SimpleSymbol("dispatchEvent").readResolve());
    static final SimpleSymbol Lit145 = ((SimpleSymbol) new SimpleSymbol("dispatchGenericEvent").readResolve());
    static final SimpleSymbol Lit146 = ((SimpleSymbol) new SimpleSymbol("lookup-handler").readResolve());
    static final SimpleSymbol Lit15 = ((SimpleSymbol) new SimpleSymbol("BackgroundImage").readResolve());
    static final SimpleSymbol Lit16 = ((SimpleSymbol) new SimpleSymbol("CloseScreenAnimation").readResolve());
    static final SimpleSymbol Lit17 = ((SimpleSymbol) new SimpleSymbol("Icon").readResolve());
    static final SimpleSymbol Lit18 = ((SimpleSymbol) new SimpleSymbol("OpenScreenAnimation").readResolve());
    static final SimpleSymbol Lit19 = ((SimpleSymbol) new SimpleSymbol("ScreenOrientation").readResolve());
    static final SimpleSymbol Lit2 = ((SimpleSymbol) new SimpleSymbol("*the-null-value*").readResolve());
    static final SimpleSymbol Lit20 = ((SimpleSymbol) new SimpleSymbol("ShowListsAsJson").readResolve());
    static final SimpleSymbol Lit21 = ((SimpleSymbol) new SimpleSymbol("Sizing").readResolve());
    static final SimpleSymbol Lit22 = ((SimpleSymbol) new SimpleSymbol("Theme").readResolve());
    static final SimpleSymbol Lit23 = ((SimpleSymbol) new SimpleSymbol("Title").readResolve());
    static final FString Lit24 = new FString("com.google.appinventor.components.runtime.VerticalArrangement");
    static final SimpleSymbol Lit25 = ((SimpleSymbol) new SimpleSymbol("VerticalArrangement1").readResolve());
    static final SimpleSymbol Lit26 = ((SimpleSymbol) new SimpleSymbol("BackgroundColor").readResolve());
    static final IntNum Lit27 = IntNum.make(16777215);
    static final SimpleSymbol Lit28 = ((SimpleSymbol) new SimpleSymbol("Height").readResolve());
    static final IntNum Lit29 = IntNum.make(-2);
    static final SimpleSymbol Lit3 = ((SimpleSymbol) new SimpleSymbol("g$email").readResolve());
    static final SimpleSymbol Lit30 = ((SimpleSymbol) new SimpleSymbol(Component.LISTVIEW_KEY_IMAGE).readResolve());
    static final SimpleSymbol Lit31 = ((SimpleSymbol) new SimpleSymbol("Width").readResolve());
    static final FString Lit32 = new FString("com.google.appinventor.components.runtime.VerticalArrangement");
    static final FString Lit33 = new FString("com.google.appinventor.components.runtime.HorizontalArrangement");
    static final SimpleSymbol Lit34 = ((SimpleSymbol) new SimpleSymbol("HorizontalArrangement1").readResolve());
    static final IntNum Lit35 = IntNum.make(16777215);
    static final FString Lit36 = new FString("com.google.appinventor.components.runtime.HorizontalArrangement");
    static final FString Lit37 = new FString("com.google.appinventor.components.runtime.ListPicker");
    static final SimpleSymbol Lit38 = ((SimpleSymbol) new SimpleSymbol("ListPicker1").readResolve());
    static final IntNum Lit39;
    static final SimpleSymbol Lit4 = ((SimpleSymbol) new SimpleSymbol("emailTB").readResolve());
    static final SimpleSymbol Lit40 = ((SimpleSymbol) new SimpleSymbol("FontSize").readResolve());
    static final IntNum Lit41 = IntNum.make(18);
    static final SimpleSymbol Lit42 = ((SimpleSymbol) new SimpleSymbol("FontTypeface").readResolve());
    static final SimpleSymbol Lit43 = ((SimpleSymbol) new SimpleSymbol("Shape").readResolve());
    static final IntNum Lit44 = IntNum.make(1);
    static final FString Lit45 = new FString("com.google.appinventor.components.runtime.ListPicker");
    static final SimpleSymbol Lit46 = ((SimpleSymbol) new SimpleSymbol("Elements").readResolve());
    static final SimpleSymbol Lit47 = ((SimpleSymbol) new SimpleSymbol("BluetoothClient1").readResolve());
    static final SimpleSymbol Lit48 = ((SimpleSymbol) new SimpleSymbol("AddressesAndNames").readResolve());
    static final SimpleSymbol Lit49;
    static final SimpleSymbol Lit5 = ((SimpleSymbol) new SimpleSymbol("Text").readResolve());
    static final SimpleSymbol Lit50 = ((SimpleSymbol) new SimpleSymbol("ListPicker1$BeforePicking").readResolve());
    static final SimpleSymbol Lit51 = ((SimpleSymbol) new SimpleSymbol("BeforePicking").readResolve());
    static final SimpleSymbol Lit52 = ((SimpleSymbol) new SimpleSymbol("Connect").readResolve());
    static final SimpleSymbol Lit53 = ((SimpleSymbol) new SimpleSymbol("Selection").readResolve());
    static final PairWithPosition Lit54 = PairWithPosition.make(Lit14, LList.Empty, "/tmp/1633789898217_0.8849879556365526-0/youngandroidproject/../src/appinventor/ai_aish_akshu/HeartRateMonitor/Screen1.yail", 295032);
    static final SimpleSymbol Lit55 = ((SimpleSymbol) new SimpleSymbol("Notifier1").readResolve());
    static final SimpleSymbol Lit56 = ((SimpleSymbol) new SimpleSymbol("LogInfo").readResolve());
    static final PairWithPosition Lit57 = PairWithPosition.make(Lit14, LList.Empty, "/tmp/1633789898217_0.8849879556365526-0/youngandroidproject/../src/appinventor/ai_aish_akshu/HeartRateMonitor/Screen1.yail", 295136);
    static final SimpleSymbol Lit58 = ((SimpleSymbol) new SimpleSymbol("ListPicker1$AfterPicking").readResolve());
    static final SimpleSymbol Lit59 = ((SimpleSymbol) new SimpleSymbol("AfterPicking").readResolve());
    static final SimpleSymbol Lit6 = ((SimpleSymbol) new SimpleSymbol("ActionBar").readResolve());
    static final FString Lit60 = new FString("com.google.appinventor.components.runtime.Button");
    static final SimpleSymbol Lit61 = ((SimpleSymbol) new SimpleSymbol("disconnectBT").readResolve());
    static final IntNum Lit62;
    static final FString Lit63 = new FString("com.google.appinventor.components.runtime.Button");
    static final SimpleSymbol Lit64 = ((SimpleSymbol) new SimpleSymbol("Disconnect").readResolve());
    static final PairWithPosition Lit65 = PairWithPosition.make(Lit14, LList.Empty, "/tmp/1633789898217_0.8849879556365526-0/youngandroidproject/../src/appinventor/ai_aish_akshu/HeartRateMonitor/Screen1.yail", 372910);
    static final SimpleSymbol Lit66 = ((SimpleSymbol) new SimpleSymbol("disconnectBT$Click").readResolve());
    static final SimpleSymbol Lit67 = ((SimpleSymbol) new SimpleSymbol("Click").readResolve());
    static final FString Lit68 = new FString("com.google.appinventor.components.runtime.Label");
    static final SimpleSymbol Lit69 = ((SimpleSymbol) new SimpleSymbol("nameLabel").readResolve());
    static final SimpleSymbol Lit7;
    static final SimpleSymbol Lit70 = ((SimpleSymbol) new SimpleSymbol("FontItalic").readResolve());
    static final IntNum Lit71 = IntNum.make(28);
    static final SimpleSymbol Lit72 = ((SimpleSymbol) new SimpleSymbol("TextColor").readResolve());
    static final IntNum Lit73;
    static final FString Lit74 = new FString("com.google.appinventor.components.runtime.Label");
    static final FString Lit75 = new FString("com.google.appinventor.components.runtime.TextBox");
    static final SimpleSymbol Lit76 = ((SimpleSymbol) new SimpleSymbol("nameTB").readResolve());
    static final SimpleSymbol Lit77 = ((SimpleSymbol) new SimpleSymbol("Hint").readResolve());
    static final FString Lit78 = new FString("com.google.appinventor.components.runtime.TextBox");
    static final FString Lit79 = new FString("com.google.appinventor.components.runtime.Label");
    static final SimpleSymbol Lit8 = ((SimpleSymbol) new SimpleSymbol("AlignHorizontal").readResolve());
    static final SimpleSymbol Lit80 = ((SimpleSymbol) new SimpleSymbol("emailLabel").readResolve());
    static final IntNum Lit81;
    static final FString Lit82 = new FString("com.google.appinventor.components.runtime.Label");
    static final FString Lit83 = new FString("com.google.appinventor.components.runtime.TextBox");
    static final FString Lit84 = new FString("com.google.appinventor.components.runtime.TextBox");
    static final FString Lit85 = new FString("com.google.appinventor.components.runtime.Button");
    static final SimpleSymbol Lit86 = ((SimpleSymbol) new SimpleSymbol("startBT").readResolve());
    static final IntNum Lit87;
    static final IntNum Lit88 = IntNum.make(24);
    static final IntNum Lit89;
    static final IntNum Lit9 = IntNum.make(3);
    static final FString Lit90 = new FString("com.google.appinventor.components.runtime.Button");
    static final SimpleSymbol Lit91 = ((SimpleSymbol) new SimpleSymbol("IsConnected").readResolve());
    static final SimpleSymbol Lit92 = ((SimpleSymbol) new SimpleSymbol("SendText").readResolve());
    static final PairWithPosition Lit93 = PairWithPosition.make(Lit14, LList.Empty, "/tmp/1633789898217_0.8849879556365526-0/youngandroidproject/../src/appinventor/ai_aish_akshu/HeartRateMonitor/Screen1.yail", 696461);
    static final SimpleSymbol Lit94 = ((SimpleSymbol) new SimpleSymbol("startBT$Click").readResolve());
    static final FString Lit95 = new FString("com.google.appinventor.components.runtime.HorizontalArrangement");
    static final SimpleSymbol Lit96 = ((SimpleSymbol) new SimpleSymbol("HorizontalArrangement2").readResolve());
    static final IntNum Lit97 = IntNum.make(16777215);
    static final FString Lit98 = new FString("com.google.appinventor.components.runtime.HorizontalArrangement");
    static final FString Lit99 = new FString("com.google.appinventor.components.runtime.Label");
    public static Screen1 Screen1;
    static final ModuleMethod lambda$Fn1 = null;
    static final ModuleMethod lambda$Fn10 = null;
    static final ModuleMethod lambda$Fn11 = null;
    static final ModuleMethod lambda$Fn12 = null;
    static final ModuleMethod lambda$Fn13 = null;
    static final ModuleMethod lambda$Fn14 = null;
    static final ModuleMethod lambda$Fn15 = null;
    static final ModuleMethod lambda$Fn16 = null;
    static final ModuleMethod lambda$Fn17 = null;
    static final ModuleMethod lambda$Fn18 = null;
    static final ModuleMethod lambda$Fn19 = null;
    static final ModuleMethod lambda$Fn2 = null;
    static final ModuleMethod lambda$Fn20 = null;
    static final ModuleMethod lambda$Fn21 = null;
    static final ModuleMethod lambda$Fn22 = null;
    static final ModuleMethod lambda$Fn23 = null;
    static final ModuleMethod lambda$Fn24 = null;
    static final ModuleMethod lambda$Fn25 = null;
    static final ModuleMethod lambda$Fn26 = null;
    static final ModuleMethod lambda$Fn27 = null;
    static final ModuleMethod lambda$Fn28 = null;
    static final ModuleMethod lambda$Fn29 = null;
    static final ModuleMethod lambda$Fn3 = null;
    static final ModuleMethod lambda$Fn4 = null;
    static final ModuleMethod lambda$Fn5 = null;
    static final ModuleMethod lambda$Fn6 = null;
    static final ModuleMethod lambda$Fn7 = null;
    static final ModuleMethod lambda$Fn8 = null;
    static final ModuleMethod lambda$Fn9 = null;
    public Boolean $Stdebug$Mnform$St;
    public final ModuleMethod $define;
    public BluetoothClient BluetoothClient1;
    public Clock Clock1;
    public final ModuleMethod Clock1$Timer;
    public HorizontalArrangement HorizontalArrangement1;
    public HorizontalArrangement HorizontalArrangement2;
    public ListPicker ListPicker1;
    public final ModuleMethod ListPicker1$AfterPicking;
    public final ModuleMethod ListPicker1$BeforePicking;
    public Notifier Notifier1;
    public C0692SmtpClient SmtpClient1;
    public VerticalArrangement VerticalArrangement1;
    public final ModuleMethod add$Mnto$Mncomponents;
    public final ModuleMethod add$Mnto$Mnevents;
    public final ModuleMethod add$Mnto$Mnform$Mndo$Mnafter$Mncreation;
    public final ModuleMethod add$Mnto$Mnform$Mnenvironment;
    public final ModuleMethod add$Mnto$Mnglobal$Mnvar$Mnenvironment;
    public final ModuleMethod add$Mnto$Mnglobal$Mnvars;
    public final ModuleMethod android$Mnlog$Mnform;
    public LList components$Mnto$Mncreate;
    public Button disconnectBT;
    public final ModuleMethod disconnectBT$Click;
    public final ModuleMethod dispatchEvent;
    public final ModuleMethod dispatchGenericEvent;
    public Label emailLabel;
    public TextBox emailTB;
    public LList events$Mnto$Mnregister;
    public LList form$Mndo$Mnafter$Mncreation;
    public Environment form$Mnenvironment;
    public Symbol form$Mnname$Mnsymbol;
    public final ModuleMethod get$Mnsimple$Mnname;
    public Environment global$Mnvar$Mnenvironment;
    public LList global$Mnvars$Mnto$Mncreate;
    public final ModuleMethod is$Mnbound$Mnin$Mnform$Mnenvironment;
    public final ModuleMethod lookup$Mnhandler;
    public final ModuleMethod lookup$Mnin$Mnform$Mnenvironment;
    public Label nameLabel;
    public TextBox nameTB;
    public final ModuleMethod onCreate;
    public final ModuleMethod process$Mnexception;
    public Label result;
    public Label resultLabel;
    public final ModuleMethod send$Mnerror;
    public Button startBT;
    public final ModuleMethod startBT$Click;

    static {
        SimpleSymbol simpleSymbol = (SimpleSymbol) new SimpleSymbol(PropertyTypeConstants.PROPERTY_TYPE_TEXT).readResolve();
        Lit14 = simpleSymbol;
        SimpleSymbol simpleSymbol2 = Lit14;
        SimpleSymbol simpleSymbol3 = (SimpleSymbol) new SimpleSymbol("number").readResolve();
        Lit10 = simpleSymbol3;
        SimpleSymbol simpleSymbol4 = Lit14;
        SimpleSymbol simpleSymbol5 = Lit14;
        SimpleSymbol simpleSymbol6 = Lit14;
        SimpleSymbol simpleSymbol7 = (SimpleSymbol) new SimpleSymbol("list").readResolve();
        Lit49 = simpleSymbol7;
        SimpleSymbol simpleSymbol8 = Lit49;
        SimpleSymbol simpleSymbol9 = Lit49;
        SimpleSymbol simpleSymbol10 = Lit14;
        SimpleSymbol simpleSymbol11 = Lit14;
        SimpleSymbol simpleSymbol12 = (SimpleSymbol) new SimpleSymbol(PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN).readResolve();
        Lit7 = simpleSymbol12;
        Lit126 = PairWithPosition.make(simpleSymbol, PairWithPosition.make(simpleSymbol2, PairWithPosition.make(simpleSymbol3, PairWithPosition.make(simpleSymbol4, PairWithPosition.make(simpleSymbol5, PairWithPosition.make(simpleSymbol6, PairWithPosition.make(simpleSymbol7, PairWithPosition.make(simpleSymbol8, PairWithPosition.make(simpleSymbol9, PairWithPosition.make(simpleSymbol10, PairWithPosition.make(simpleSymbol11, PairWithPosition.make(simpleSymbol12, PairWithPosition.make(Lit49, LList.Empty, "/tmp/1633789898217_0.8849879556365526-0/youngandroidproject/../src/appinventor/ai_aish_akshu/HeartRateMonitor/Screen1.yail", 906643), "/tmp/1633789898217_0.8849879556365526-0/youngandroidproject/../src/appinventor/ai_aish_akshu/HeartRateMonitor/Screen1.yail", 906635), "/tmp/1633789898217_0.8849879556365526-0/youngandroidproject/../src/appinventor/ai_aish_akshu/HeartRateMonitor/Screen1.yail", 906630), "/tmp/1633789898217_0.8849879556365526-0/youngandroidproject/../src/appinventor/ai_aish_akshu/HeartRateMonitor/Screen1.yail", 906625), "/tmp/1633789898217_0.8849879556365526-0/youngandroidproject/../src/appinventor/ai_aish_akshu/HeartRateMonitor/Screen1.yail", 906620), "/tmp/1633789898217_0.8849879556365526-0/youngandroidproject/../src/appinventor/ai_aish_akshu/HeartRateMonitor/Screen1.yail", 906615), "/tmp/1633789898217_0.8849879556365526-0/youngandroidproject/../src/appinventor/ai_aish_akshu/HeartRateMonitor/Screen1.yail", 906610), "/tmp/1633789898217_0.8849879556365526-0/youngandroidproject/../src/appinventor/ai_aish_akshu/HeartRateMonitor/Screen1.yail", 906605), "/tmp/1633789898217_0.8849879556365526-0/youngandroidproject/../src/appinventor/ai_aish_akshu/HeartRateMonitor/Screen1.yail", 906600), "/tmp/1633789898217_0.8849879556365526-0/youngandroidproject/../src/appinventor/ai_aish_akshu/HeartRateMonitor/Screen1.yail", 906595), "/tmp/1633789898217_0.8849879556365526-0/youngandroidproject/../src/appinventor/ai_aish_akshu/HeartRateMonitor/Screen1.yail", 906588), "/tmp/1633789898217_0.8849879556365526-0/youngandroidproject/../src/appinventor/ai_aish_akshu/HeartRateMonitor/Screen1.yail", 906583), "/tmp/1633789898217_0.8849879556365526-0/youngandroidproject/../src/appinventor/ai_aish_akshu/HeartRateMonitor/Screen1.yail", 906577);
        int[] iArr = new int[2];
        iArr[0] = -16777216;
        Lit89 = IntNum.make(iArr);
        int[] iArr2 = new int[2];
        iArr2[0] = -26319;
        Lit87 = IntNum.make(iArr2);
        int[] iArr3 = new int[2];
        iArr3[0] = -14336;
        Lit81 = IntNum.make(iArr3);
        int[] iArr4 = new int[2];
        iArr4[0] = -14336;
        Lit73 = IntNum.make(iArr4);
        int[] iArr5 = new int[2];
        iArr5[0] = -4180683;
        Lit62 = IntNum.make(iArr5);
        int[] iArr6 = new int[2];
        iArr6[0] = -15356605;
        Lit39 = IntNum.make(iArr6);
    }

    public Screen1() {
        ModuleInfo.register(this);
        frame frame2 = new frame();
        frame2.$main = this;
        this.get$Mnsimple$Mnname = new ModuleMethod(frame2, 1, Lit133, FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        this.onCreate = new ModuleMethod(frame2, 2, "onCreate", FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        this.android$Mnlog$Mnform = new ModuleMethod(frame2, 3, Lit134, FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        this.add$Mnto$Mnform$Mnenvironment = new ModuleMethod(frame2, 4, Lit135, 8194);
        this.lookup$Mnin$Mnform$Mnenvironment = new ModuleMethod(frame2, 5, Lit136, 8193);
        this.is$Mnbound$Mnin$Mnform$Mnenvironment = new ModuleMethod(frame2, 7, Lit137, FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        this.add$Mnto$Mnglobal$Mnvar$Mnenvironment = new ModuleMethod(frame2, 8, Lit138, 8194);
        this.add$Mnto$Mnevents = new ModuleMethod(frame2, 9, Lit139, 8194);
        this.add$Mnto$Mncomponents = new ModuleMethod(frame2, 10, Lit140, 16388);
        this.add$Mnto$Mnglobal$Mnvars = new ModuleMethod(frame2, 11, Lit141, 8194);
        this.add$Mnto$Mnform$Mndo$Mnafter$Mncreation = new ModuleMethod(frame2, 12, Lit142, FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        this.send$Mnerror = new ModuleMethod(frame2, 13, Lit143, FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        this.process$Mnexception = new ModuleMethod(frame2, 14, "process-exception", FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        this.dispatchEvent = new ModuleMethod(frame2, 15, Lit144, 16388);
        this.dispatchGenericEvent = new ModuleMethod(frame2, 16, Lit145, 16388);
        this.lookup$Mnhandler = new ModuleMethod(frame2, 17, Lit146, 8194);
        ModuleMethod moduleMethod = new ModuleMethod(frame2, 18, (Object) null, 0);
        moduleMethod.setProperty("source-location", "/tmp/runtime8267242385442957401.scm:627");
        lambda$Fn1 = moduleMethod;
        this.$define = new ModuleMethod(frame2, 19, "$define", 0);
        lambda$Fn2 = new ModuleMethod(frame2, 20, (Object) null, 0);
        lambda$Fn3 = new ModuleMethod(frame2, 21, (Object) null, 0);
        lambda$Fn4 = new ModuleMethod(frame2, 22, (Object) null, 0);
        lambda$Fn5 = new ModuleMethod(frame2, 23, (Object) null, 0);
        lambda$Fn6 = new ModuleMethod(frame2, 24, (Object) null, 0);
        lambda$Fn7 = new ModuleMethod(frame2, 25, (Object) null, 0);
        lambda$Fn8 = new ModuleMethod(frame2, 26, (Object) null, 0);
        lambda$Fn9 = new ModuleMethod(frame2, 27, (Object) null, 0);
        this.ListPicker1$BeforePicking = new ModuleMethod(frame2, 28, Lit50, 0);
        this.ListPicker1$AfterPicking = new ModuleMethod(frame2, 29, Lit58, 0);
        lambda$Fn10 = new ModuleMethod(frame2, 30, (Object) null, 0);
        lambda$Fn11 = new ModuleMethod(frame2, 31, (Object) null, 0);
        this.disconnectBT$Click = new ModuleMethod(frame2, 32, Lit66, 0);
        lambda$Fn12 = new ModuleMethod(frame2, 33, (Object) null, 0);
        lambda$Fn13 = new ModuleMethod(frame2, 34, (Object) null, 0);
        lambda$Fn14 = new ModuleMethod(frame2, 35, (Object) null, 0);
        lambda$Fn15 = new ModuleMethod(frame2, 36, (Object) null, 0);
        lambda$Fn16 = new ModuleMethod(frame2, 37, (Object) null, 0);
        lambda$Fn17 = new ModuleMethod(frame2, 38, (Object) null, 0);
        lambda$Fn18 = new ModuleMethod(frame2, 39, (Object) null, 0);
        lambda$Fn19 = new ModuleMethod(frame2, 40, (Object) null, 0);
        lambda$Fn20 = new ModuleMethod(frame2, 41, (Object) null, 0);
        lambda$Fn21 = new ModuleMethod(frame2, 42, (Object) null, 0);
        this.startBT$Click = new ModuleMethod(frame2, 43, Lit94, 0);
        lambda$Fn22 = new ModuleMethod(frame2, 44, (Object) null, 0);
        lambda$Fn23 = new ModuleMethod(frame2, 45, (Object) null, 0);
        lambda$Fn24 = new ModuleMethod(frame2, 46, (Object) null, 0);
        lambda$Fn25 = new ModuleMethod(frame2, 47, (Object) null, 0);
        lambda$Fn26 = new ModuleMethod(frame2, 48, (Object) null, 0);
        lambda$Fn27 = new ModuleMethod(frame2, 49, (Object) null, 0);
        lambda$Fn28 = new ModuleMethod(frame2, 50, (Object) null, 0);
        lambda$Fn29 = new ModuleMethod(frame2, 51, (Object) null, 0);
        this.Clock1$Timer = new ModuleMethod(frame2, 52, Lit127, 0);
    }

    public Object lookupInFormEnvironment(Symbol symbol) {
        return lookupInFormEnvironment(symbol, Boolean.FALSE);
    }

    public void run() {
        CallContext instance = CallContext.getInstance();
        Consumer consumer = instance.consumer;
        instance.consumer = VoidConsumer.instance;
        try {
            run(instance);
            th = null;
        } catch (Throwable th) {
            th = th;
        }
        ModuleBody.runCleanup(instance, th, consumer);
    }

    public final void run(CallContext $ctx) {
        String obj;
        Consumer $result = $ctx.consumer;
        Object find = require.find("com.google.youngandroid.runtime");
        try {
            ((Runnable) find).run();
            this.$Stdebug$Mnform$St = Boolean.FALSE;
            this.form$Mnenvironment = Environment.make(misc.symbol$To$String(Lit0));
            FString stringAppend = strings.stringAppend(misc.symbol$To$String(Lit0), "-global-vars");
            if (stringAppend == null) {
                obj = null;
            } else {
                obj = stringAppend.toString();
            }
            this.global$Mnvar$Mnenvironment = Environment.make(obj);
            Screen1 = null;
            this.form$Mnname$Mnsymbol = Lit0;
            this.events$Mnto$Mnregister = LList.Empty;
            this.components$Mnto$Mncreate = LList.Empty;
            this.global$Mnvars$Mnto$Mncreate = LList.Empty;
            this.form$Mndo$Mnafter$Mncreation = LList.Empty;
            Object find2 = require.find("com.google.youngandroid.runtime");
            try {
                ((Runnable) find2).run();
                if (C0642runtime.$Stthis$Mnis$Mnthe$Mnrepl$St != Boolean.FALSE) {
                    Values.writeValues(C0642runtime.addGlobalVarToCurrentFormEnvironment(Lit3, C0642runtime.getProperty$1(Lit4, Lit5)), $result);
                } else {
                    addToGlobalVars(Lit3, lambda$Fn2);
                }
                if (C0642runtime.$Stthis$Mnis$Mnthe$Mnrepl$St != Boolean.FALSE) {
                    C0642runtime.setAndCoerceProperty$Ex(Lit0, Lit6, Boolean.TRUE, Lit7);
                    C0642runtime.setAndCoerceProperty$Ex(Lit0, Lit8, Lit9, Lit10);
                    C0642runtime.setAndCoerceProperty$Ex(Lit0, Lit11, Lit12, Lit10);
                    C0642runtime.setAndCoerceProperty$Ex(Lit0, Lit13, "HeartRateMonitor", Lit14);
                    C0642runtime.setAndCoerceProperty$Ex(Lit0, Lit15, "appbg.jpg", Lit14);
                    C0642runtime.setAndCoerceProperty$Ex(Lit0, Lit16, "zoom", Lit14);
                    C0642runtime.setAndCoerceProperty$Ex(Lit0, Lit17, "3b366993aed64ba49dc0a74ac59271d6.png", Lit14);
                    C0642runtime.setAndCoerceProperty$Ex(Lit0, Lit18, "zoom", Lit14);
                    C0642runtime.setAndCoerceProperty$Ex(Lit0, Lit19, "portrait", Lit14);
                    C0642runtime.setAndCoerceProperty$Ex(Lit0, Lit20, Boolean.TRUE, Lit7);
                    C0642runtime.setAndCoerceProperty$Ex(Lit0, Lit21, "Responsive", Lit14);
                    C0642runtime.setAndCoerceProperty$Ex(Lit0, Lit22, "AppTheme", Lit14);
                    Values.writeValues(C0642runtime.setAndCoerceProperty$Ex(Lit0, Lit23, "Heart Rate Monitor", Lit14), $result);
                } else {
                    addToFormDoAfterCreation(new Promise(lambda$Fn3));
                }
                this.VerticalArrangement1 = null;
                if (C0642runtime.$Stthis$Mnis$Mnthe$Mnrepl$St != Boolean.FALSE) {
                    Values.writeValues(C0642runtime.addComponentWithinRepl(Lit0, Lit24, Lit25, lambda$Fn4), $result);
                } else {
                    addToComponents(Lit0, Lit32, Lit25, lambda$Fn5);
                }
                this.HorizontalArrangement1 = null;
                if (C0642runtime.$Stthis$Mnis$Mnthe$Mnrepl$St != Boolean.FALSE) {
                    Values.writeValues(C0642runtime.addComponentWithinRepl(Lit25, Lit33, Lit34, lambda$Fn6), $result);
                } else {
                    addToComponents(Lit25, Lit36, Lit34, lambda$Fn7);
                }
                this.ListPicker1 = null;
                if (C0642runtime.$Stthis$Mnis$Mnthe$Mnrepl$St != Boolean.FALSE) {
                    Values.writeValues(C0642runtime.addComponentWithinRepl(Lit34, Lit37, Lit38, lambda$Fn8), $result);
                } else {
                    addToComponents(Lit34, Lit45, Lit38, lambda$Fn9);
                }
                if (C0642runtime.$Stthis$Mnis$Mnthe$Mnrepl$St != Boolean.FALSE) {
                    C0642runtime.addToCurrentFormEnvironment(Lit50, this.ListPicker1$BeforePicking);
                } else {
                    addToFormEnvironment(Lit50, this.ListPicker1$BeforePicking);
                }
                if (C0642runtime.$Stthis$Mnis$Mnthe$Mnrepl$St != Boolean.FALSE) {
                    EventDispatcher.registerEventForDelegation((HandlesEventDispatching) C0642runtime.$Stthis$Mnform$St, "ListPicker1", "BeforePicking");
                } else {
                    addToEvents(Lit38, Lit51);
                }
                if (C0642runtime.$Stthis$Mnis$Mnthe$Mnrepl$St != Boolean.FALSE) {
                    C0642runtime.addToCurrentFormEnvironment(Lit58, this.ListPicker1$AfterPicking);
                } else {
                    addToFormEnvironment(Lit58, this.ListPicker1$AfterPicking);
                }
                if (C0642runtime.$Stthis$Mnis$Mnthe$Mnrepl$St != Boolean.FALSE) {
                    EventDispatcher.registerEventForDelegation((HandlesEventDispatching) C0642runtime.$Stthis$Mnform$St, "ListPicker1", "AfterPicking");
                } else {
                    addToEvents(Lit38, Lit59);
                }
                this.disconnectBT = null;
                if (C0642runtime.$Stthis$Mnis$Mnthe$Mnrepl$St != Boolean.FALSE) {
                    Values.writeValues(C0642runtime.addComponentWithinRepl(Lit34, Lit60, Lit61, lambda$Fn10), $result);
                } else {
                    addToComponents(Lit34, Lit63, Lit61, lambda$Fn11);
                }
                if (C0642runtime.$Stthis$Mnis$Mnthe$Mnrepl$St != Boolean.FALSE) {
                    C0642runtime.addToCurrentFormEnvironment(Lit66, this.disconnectBT$Click);
                } else {
                    addToFormEnvironment(Lit66, this.disconnectBT$Click);
                }
                if (C0642runtime.$Stthis$Mnis$Mnthe$Mnrepl$St != Boolean.FALSE) {
                    EventDispatcher.registerEventForDelegation((HandlesEventDispatching) C0642runtime.$Stthis$Mnform$St, "disconnectBT", "Click");
                } else {
                    addToEvents(Lit61, Lit67);
                }
                this.nameLabel = null;
                if (C0642runtime.$Stthis$Mnis$Mnthe$Mnrepl$St != Boolean.FALSE) {
                    Values.writeValues(C0642runtime.addComponentWithinRepl(Lit25, Lit68, Lit69, lambda$Fn12), $result);
                } else {
                    addToComponents(Lit25, Lit74, Lit69, lambda$Fn13);
                }
                this.nameTB = null;
                if (C0642runtime.$Stthis$Mnis$Mnthe$Mnrepl$St != Boolean.FALSE) {
                    Values.writeValues(C0642runtime.addComponentWithinRepl(Lit25, Lit75, Lit76, lambda$Fn14), $result);
                } else {
                    addToComponents(Lit25, Lit78, Lit76, lambda$Fn15);
                }
                this.emailLabel = null;
                if (C0642runtime.$Stthis$Mnis$Mnthe$Mnrepl$St != Boolean.FALSE) {
                    Values.writeValues(C0642runtime.addComponentWithinRepl(Lit25, Lit79, Lit80, lambda$Fn16), $result);
                } else {
                    addToComponents(Lit25, Lit82, Lit80, lambda$Fn17);
                }
                this.emailTB = null;
                if (C0642runtime.$Stthis$Mnis$Mnthe$Mnrepl$St != Boolean.FALSE) {
                    Values.writeValues(C0642runtime.addComponentWithinRepl(Lit25, Lit83, Lit4, lambda$Fn18), $result);
                } else {
                    addToComponents(Lit25, Lit84, Lit4, lambda$Fn19);
                }
                this.startBT = null;
                if (C0642runtime.$Stthis$Mnis$Mnthe$Mnrepl$St != Boolean.FALSE) {
                    Values.writeValues(C0642runtime.addComponentWithinRepl(Lit25, Lit85, Lit86, lambda$Fn20), $result);
                } else {
                    addToComponents(Lit25, Lit90, Lit86, lambda$Fn21);
                }
                if (C0642runtime.$Stthis$Mnis$Mnthe$Mnrepl$St != Boolean.FALSE) {
                    C0642runtime.addToCurrentFormEnvironment(Lit94, this.startBT$Click);
                } else {
                    addToFormEnvironment(Lit94, this.startBT$Click);
                }
                if (C0642runtime.$Stthis$Mnis$Mnthe$Mnrepl$St != Boolean.FALSE) {
                    EventDispatcher.registerEventForDelegation((HandlesEventDispatching) C0642runtime.$Stthis$Mnform$St, "startBT", "Click");
                } else {
                    addToEvents(Lit86, Lit67);
                }
                this.HorizontalArrangement2 = null;
                if (C0642runtime.$Stthis$Mnis$Mnthe$Mnrepl$St != Boolean.FALSE) {
                    Values.writeValues(C0642runtime.addComponentWithinRepl(Lit25, Lit95, Lit96, lambda$Fn22), $result);
                } else {
                    addToComponents(Lit25, Lit98, Lit96, lambda$Fn23);
                }
                this.resultLabel = null;
                if (C0642runtime.$Stthis$Mnis$Mnthe$Mnrepl$St != Boolean.FALSE) {
                    Values.writeValues(C0642runtime.addComponentWithinRepl(Lit96, Lit99, Lit100, lambda$Fn24), $result);
                } else {
                    addToComponents(Lit96, Lit102, Lit100, lambda$Fn25);
                }
                this.result = null;
                if (C0642runtime.$Stthis$Mnis$Mnthe$Mnrepl$St != Boolean.FALSE) {
                    Values.writeValues(C0642runtime.addComponentWithinRepl(Lit96, Lit103, Lit104, lambda$Fn26), $result);
                } else {
                    addToComponents(Lit96, Lit108, Lit104, lambda$Fn27);
                }
                this.BluetoothClient1 = null;
                if (C0642runtime.$Stthis$Mnis$Mnthe$Mnrepl$St != Boolean.FALSE) {
                    Values.writeValues(C0642runtime.addComponentWithinRepl(Lit0, Lit109, Lit47, Boolean.FALSE), $result);
                } else {
                    addToComponents(Lit0, Lit110, Lit47, Boolean.FALSE);
                }
                this.Clock1 = null;
                if (C0642runtime.$Stthis$Mnis$Mnthe$Mnrepl$St != Boolean.FALSE) {
                    Values.writeValues(C0642runtime.addComponentWithinRepl(Lit0, Lit111, Lit112, Boolean.FALSE), $result);
                } else {
                    addToComponents(Lit0, Lit113, Lit112, Boolean.FALSE);
                }
                if (C0642runtime.$Stthis$Mnis$Mnthe$Mnrepl$St != Boolean.FALSE) {
                    C0642runtime.addToCurrentFormEnvironment(Lit127, this.Clock1$Timer);
                } else {
                    addToFormEnvironment(Lit127, this.Clock1$Timer);
                }
                if (C0642runtime.$Stthis$Mnis$Mnthe$Mnrepl$St != Boolean.FALSE) {
                    EventDispatcher.registerEventForDelegation((HandlesEventDispatching) C0642runtime.$Stthis$Mnform$St, "Clock1", "Timer");
                } else {
                    addToEvents(Lit112, Lit128);
                }
                this.Notifier1 = null;
                if (C0642runtime.$Stthis$Mnis$Mnthe$Mnrepl$St != Boolean.FALSE) {
                    Values.writeValues(C0642runtime.addComponentWithinRepl(Lit0, Lit129, Lit55, Boolean.FALSE), $result);
                } else {
                    addToComponents(Lit0, Lit130, Lit55, Boolean.FALSE);
                }
                this.SmtpClient1 = null;
                if (C0642runtime.$Stthis$Mnis$Mnthe$Mnrepl$St != Boolean.FALSE) {
                    Values.writeValues(C0642runtime.addComponentWithinRepl(Lit0, Lit131, Lit119, Boolean.FALSE), $result);
                } else {
                    addToComponents(Lit0, Lit132, Lit119, Boolean.FALSE);
                }
                C0642runtime.initRuntime();
            } catch (ClassCastException e) {
                throw new WrongType(e, "java.lang.Runnable.run()", 1, find2);
            }
        } catch (ClassCastException e2) {
            throw new WrongType(e2, "java.lang.Runnable.run()", 1, find);
        }
    }

    static Object lambda3() {
        return C0642runtime.getProperty$1(Lit4, Lit5);
    }

    static Object lambda4() {
        C0642runtime.setAndCoerceProperty$Ex(Lit0, Lit6, Boolean.TRUE, Lit7);
        C0642runtime.setAndCoerceProperty$Ex(Lit0, Lit8, Lit9, Lit10);
        C0642runtime.setAndCoerceProperty$Ex(Lit0, Lit11, Lit12, Lit10);
        C0642runtime.setAndCoerceProperty$Ex(Lit0, Lit13, "HeartRateMonitor", Lit14);
        C0642runtime.setAndCoerceProperty$Ex(Lit0, Lit15, "appbg.jpg", Lit14);
        C0642runtime.setAndCoerceProperty$Ex(Lit0, Lit16, "zoom", Lit14);
        C0642runtime.setAndCoerceProperty$Ex(Lit0, Lit17, "3b366993aed64ba49dc0a74ac59271d6.png", Lit14);
        C0642runtime.setAndCoerceProperty$Ex(Lit0, Lit18, "zoom", Lit14);
        C0642runtime.setAndCoerceProperty$Ex(Lit0, Lit19, "portrait", Lit14);
        C0642runtime.setAndCoerceProperty$Ex(Lit0, Lit20, Boolean.TRUE, Lit7);
        C0642runtime.setAndCoerceProperty$Ex(Lit0, Lit21, "Responsive", Lit14);
        C0642runtime.setAndCoerceProperty$Ex(Lit0, Lit22, "AppTheme", Lit14);
        return C0642runtime.setAndCoerceProperty$Ex(Lit0, Lit23, "Heart Rate Monitor", Lit14);
    }

    static Object lambda5() {
        C0642runtime.setAndCoerceProperty$Ex(Lit25, Lit8, Lit9, Lit10);
        C0642runtime.setAndCoerceProperty$Ex(Lit25, Lit11, Lit12, Lit10);
        C0642runtime.setAndCoerceProperty$Ex(Lit25, Lit26, Lit27, Lit10);
        C0642runtime.setAndCoerceProperty$Ex(Lit25, Lit28, Lit29, Lit10);
        C0642runtime.setAndCoerceProperty$Ex(Lit25, Lit30, "appbg.jpg", Lit14);
        return C0642runtime.setAndCoerceProperty$Ex(Lit25, Lit31, Lit29, Lit10);
    }

    static Object lambda6() {
        C0642runtime.setAndCoerceProperty$Ex(Lit25, Lit8, Lit9, Lit10);
        C0642runtime.setAndCoerceProperty$Ex(Lit25, Lit11, Lit12, Lit10);
        C0642runtime.setAndCoerceProperty$Ex(Lit25, Lit26, Lit27, Lit10);
        C0642runtime.setAndCoerceProperty$Ex(Lit25, Lit28, Lit29, Lit10);
        C0642runtime.setAndCoerceProperty$Ex(Lit25, Lit30, "appbg.jpg", Lit14);
        return C0642runtime.setAndCoerceProperty$Ex(Lit25, Lit31, Lit29, Lit10);
    }

    static Object lambda7() {
        C0642runtime.setAndCoerceProperty$Ex(Lit34, Lit26, Lit35, Lit10);
        return C0642runtime.setAndCoerceProperty$Ex(Lit34, Lit31, Lit29, Lit10);
    }

    static Object lambda8() {
        C0642runtime.setAndCoerceProperty$Ex(Lit34, Lit26, Lit35, Lit10);
        return C0642runtime.setAndCoerceProperty$Ex(Lit34, Lit31, Lit29, Lit10);
    }

    static Object lambda10() {
        C0642runtime.setAndCoerceProperty$Ex(Lit38, Lit26, Lit39, Lit10);
        C0642runtime.setAndCoerceProperty$Ex(Lit38, Lit40, Lit41, Lit10);
        C0642runtime.setAndCoerceProperty$Ex(Lit38, Lit42, Lit9, Lit10);
        C0642runtime.setAndCoerceProperty$Ex(Lit38, Lit43, Lit44, Lit10);
        C0642runtime.setAndCoerceProperty$Ex(Lit38, Lit5, "Scan", Lit14);
        return C0642runtime.setAndCoerceProperty$Ex(Lit38, Lit31, Lit29, Lit10);
    }

    static Object lambda9() {
        C0642runtime.setAndCoerceProperty$Ex(Lit38, Lit26, Lit39, Lit10);
        C0642runtime.setAndCoerceProperty$Ex(Lit38, Lit40, Lit41, Lit10);
        C0642runtime.setAndCoerceProperty$Ex(Lit38, Lit42, Lit9, Lit10);
        C0642runtime.setAndCoerceProperty$Ex(Lit38, Lit43, Lit44, Lit10);
        C0642runtime.setAndCoerceProperty$Ex(Lit38, Lit5, "Scan", Lit14);
        return C0642runtime.setAndCoerceProperty$Ex(Lit38, Lit31, Lit29, Lit10);
    }

    public Object ListPicker1$BeforePicking() {
        C0642runtime.setThisForm();
        return C0642runtime.setAndCoerceProperty$Ex(Lit38, Lit46, C0642runtime.getProperty$1(Lit47, Lit48), Lit49);
    }

    public Object ListPicker1$AfterPicking() {
        C0642runtime.setThisForm();
        return C0642runtime.callComponentMethod(Lit47, Lit52, LList.list1(C0642runtime.getProperty$1(Lit38, Lit53)), Lit54) != Boolean.FALSE ? C0642runtime.callComponentMethod(Lit55, Lit56, LList.list1("Bluetooth Conected!"), Lit57) : Values.empty;
    }

    static Object lambda11() {
        C0642runtime.setAndCoerceProperty$Ex(Lit61, Lit26, Lit62, Lit10);
        C0642runtime.setAndCoerceProperty$Ex(Lit61, Lit40, Lit41, Lit10);
        C0642runtime.setAndCoerceProperty$Ex(Lit61, Lit42, Lit9, Lit10);
        C0642runtime.setAndCoerceProperty$Ex(Lit61, Lit43, Lit44, Lit10);
        C0642runtime.setAndCoerceProperty$Ex(Lit61, Lit5, "Disconnect", Lit14);
        return C0642runtime.setAndCoerceProperty$Ex(Lit61, Lit31, Lit29, Lit10);
    }

    static Object lambda12() {
        C0642runtime.setAndCoerceProperty$Ex(Lit61, Lit26, Lit62, Lit10);
        C0642runtime.setAndCoerceProperty$Ex(Lit61, Lit40, Lit41, Lit10);
        C0642runtime.setAndCoerceProperty$Ex(Lit61, Lit42, Lit9, Lit10);
        C0642runtime.setAndCoerceProperty$Ex(Lit61, Lit43, Lit44, Lit10);
        C0642runtime.setAndCoerceProperty$Ex(Lit61, Lit5, "Disconnect", Lit14);
        return C0642runtime.setAndCoerceProperty$Ex(Lit61, Lit31, Lit29, Lit10);
    }

    public Object disconnectBT$Click() {
        C0642runtime.setThisForm();
        C0642runtime.callComponentMethod(Lit47, Lit64, LList.Empty, LList.Empty);
        return C0642runtime.callComponentMethod(Lit55, Lit56, LList.list1("Bluetooth Disconnected!"), Lit65);
    }

    static Object lambda13() {
        C0642runtime.setAndCoerceProperty$Ex(Lit69, Lit70, Boolean.TRUE, Lit7);
        C0642runtime.setAndCoerceProperty$Ex(Lit69, Lit40, Lit71, Lit10);
        C0642runtime.setAndCoerceProperty$Ex(Lit69, Lit42, Lit12, Lit10);
        C0642runtime.setAndCoerceProperty$Ex(Lit69, Lit5, "Name:", Lit14);
        C0642runtime.setAndCoerceProperty$Ex(Lit69, Lit72, Lit73, Lit10);
        return C0642runtime.setAndCoerceProperty$Ex(Lit69, Lit31, Lit29, Lit10);
    }

    static Object lambda14() {
        C0642runtime.setAndCoerceProperty$Ex(Lit69, Lit70, Boolean.TRUE, Lit7);
        C0642runtime.setAndCoerceProperty$Ex(Lit69, Lit40, Lit71, Lit10);
        C0642runtime.setAndCoerceProperty$Ex(Lit69, Lit42, Lit12, Lit10);
        C0642runtime.setAndCoerceProperty$Ex(Lit69, Lit5, "Name:", Lit14);
        C0642runtime.setAndCoerceProperty$Ex(Lit69, Lit72, Lit73, Lit10);
        return C0642runtime.setAndCoerceProperty$Ex(Lit69, Lit31, Lit29, Lit10);
    }

    static Object lambda15() {
        C0642runtime.setAndCoerceProperty$Ex(Lit76, Lit40, Lit71, Lit10);
        C0642runtime.setAndCoerceProperty$Ex(Lit76, Lit42, Lit9, Lit10);
        C0642runtime.setAndCoerceProperty$Ex(Lit76, Lit77, "Enter Name Here", Lit14);
        return C0642runtime.setAndCoerceProperty$Ex(Lit76, Lit31, Lit29, Lit10);
    }

    static Object lambda16() {
        C0642runtime.setAndCoerceProperty$Ex(Lit76, Lit40, Lit71, Lit10);
        C0642runtime.setAndCoerceProperty$Ex(Lit76, Lit42, Lit9, Lit10);
        C0642runtime.setAndCoerceProperty$Ex(Lit76, Lit77, "Enter Name Here", Lit14);
        return C0642runtime.setAndCoerceProperty$Ex(Lit76, Lit31, Lit29, Lit10);
    }

    static Object lambda17() {
        C0642runtime.setAndCoerceProperty$Ex(Lit80, Lit70, Boolean.TRUE, Lit7);
        C0642runtime.setAndCoerceProperty$Ex(Lit80, Lit40, Lit71, Lit10);
        C0642runtime.setAndCoerceProperty$Ex(Lit80, Lit42, Lit12, Lit10);
        C0642runtime.setAndCoerceProperty$Ex(Lit80, Lit5, "Email ID:", Lit14);
        C0642runtime.setAndCoerceProperty$Ex(Lit80, Lit72, Lit81, Lit10);
        return C0642runtime.setAndCoerceProperty$Ex(Lit80, Lit31, Lit29, Lit10);
    }

    static Object lambda18() {
        C0642runtime.setAndCoerceProperty$Ex(Lit80, Lit70, Boolean.TRUE, Lit7);
        C0642runtime.setAndCoerceProperty$Ex(Lit80, Lit40, Lit71, Lit10);
        C0642runtime.setAndCoerceProperty$Ex(Lit80, Lit42, Lit12, Lit10);
        C0642runtime.setAndCoerceProperty$Ex(Lit80, Lit5, "Email ID:", Lit14);
        C0642runtime.setAndCoerceProperty$Ex(Lit80, Lit72, Lit81, Lit10);
        return C0642runtime.setAndCoerceProperty$Ex(Lit80, Lit31, Lit29, Lit10);
    }

    static Object lambda19() {
        C0642runtime.setAndCoerceProperty$Ex(Lit4, Lit40, Lit71, Lit10);
        C0642runtime.setAndCoerceProperty$Ex(Lit4, Lit42, Lit9, Lit10);
        C0642runtime.setAndCoerceProperty$Ex(Lit4, Lit77, "Enter Email Here", Lit14);
        return C0642runtime.setAndCoerceProperty$Ex(Lit4, Lit31, Lit29, Lit10);
    }

    static Object lambda20() {
        C0642runtime.setAndCoerceProperty$Ex(Lit4, Lit40, Lit71, Lit10);
        C0642runtime.setAndCoerceProperty$Ex(Lit4, Lit42, Lit9, Lit10);
        C0642runtime.setAndCoerceProperty$Ex(Lit4, Lit77, "Enter Email Here", Lit14);
        return C0642runtime.setAndCoerceProperty$Ex(Lit4, Lit31, Lit29, Lit10);
    }

    static Object lambda21() {
        C0642runtime.setAndCoerceProperty$Ex(Lit86, Lit26, Lit87, Lit10);
        C0642runtime.setAndCoerceProperty$Ex(Lit86, Lit40, Lit88, Lit10);
        C0642runtime.setAndCoerceProperty$Ex(Lit86, Lit42, Lit9, Lit10);
        C0642runtime.setAndCoerceProperty$Ex(Lit86, Lit43, Lit44, Lit10);
        C0642runtime.setAndCoerceProperty$Ex(Lit86, Lit5, "Start Measurement", Lit14);
        return C0642runtime.setAndCoerceProperty$Ex(Lit86, Lit72, Lit89, Lit10);
    }

    static Object lambda22() {
        C0642runtime.setAndCoerceProperty$Ex(Lit86, Lit26, Lit87, Lit10);
        C0642runtime.setAndCoerceProperty$Ex(Lit86, Lit40, Lit88, Lit10);
        C0642runtime.setAndCoerceProperty$Ex(Lit86, Lit42, Lit9, Lit10);
        C0642runtime.setAndCoerceProperty$Ex(Lit86, Lit43, Lit44, Lit10);
        C0642runtime.setAndCoerceProperty$Ex(Lit86, Lit5, "Start Measurement", Lit14);
        return C0642runtime.setAndCoerceProperty$Ex(Lit86, Lit72, Lit89, Lit10);
    }

    public Object startBT$Click() {
        C0642runtime.setThisForm();
        return C0642runtime.getProperty$1(Lit47, Lit91) != Boolean.FALSE ? C0642runtime.callComponentMethod(Lit47, Lit92, LList.list1("1"), Lit93) : Values.empty;
    }

    static Object lambda23() {
        C0642runtime.setAndCoerceProperty$Ex(Lit96, Lit11, Lit12, Lit10);
        C0642runtime.setAndCoerceProperty$Ex(Lit96, Lit26, Lit97, Lit10);
        return C0642runtime.setAndCoerceProperty$Ex(Lit96, Lit31, Lit29, Lit10);
    }

    static Object lambda24() {
        C0642runtime.setAndCoerceProperty$Ex(Lit96, Lit11, Lit12, Lit10);
        C0642runtime.setAndCoerceProperty$Ex(Lit96, Lit26, Lit97, Lit10);
        return C0642runtime.setAndCoerceProperty$Ex(Lit96, Lit31, Lit29, Lit10);
    }

    static Object lambda25() {
        C0642runtime.setAndCoerceProperty$Ex(Lit100, Lit40, Lit101, Lit10);
        C0642runtime.setAndCoerceProperty$Ex(Lit100, Lit42, Lit12, Lit10);
        C0642runtime.setAndCoerceProperty$Ex(Lit100, Lit5, "Heart Rate (BPM):", Lit14);
        return C0642runtime.setAndCoerceProperty$Ex(Lit100, Lit31, Lit29, Lit10);
    }

    static Object lambda26() {
        C0642runtime.setAndCoerceProperty$Ex(Lit100, Lit40, Lit101, Lit10);
        C0642runtime.setAndCoerceProperty$Ex(Lit100, Lit42, Lit12, Lit10);
        C0642runtime.setAndCoerceProperty$Ex(Lit100, Lit5, "Heart Rate (BPM):", Lit14);
        return C0642runtime.setAndCoerceProperty$Ex(Lit100, Lit31, Lit29, Lit10);
    }

    static Object lambda27() {
        C0642runtime.setAndCoerceProperty$Ex(Lit104, Lit105, Boolean.TRUE, Lit7);
        C0642runtime.setAndCoerceProperty$Ex(Lit104, Lit40, Lit106, Lit10);
        C0642runtime.setAndCoerceProperty$Ex(Lit104, Lit42, Lit9, Lit10);
        C0642runtime.setAndCoerceProperty$Ex(Lit104, Lit107, Lit44, Lit10);
        return C0642runtime.setAndCoerceProperty$Ex(Lit104, Lit31, Lit29, Lit10);
    }

    static Object lambda28() {
        C0642runtime.setAndCoerceProperty$Ex(Lit104, Lit105, Boolean.TRUE, Lit7);
        C0642runtime.setAndCoerceProperty$Ex(Lit104, Lit40, Lit106, Lit10);
        C0642runtime.setAndCoerceProperty$Ex(Lit104, Lit42, Lit9, Lit10);
        C0642runtime.setAndCoerceProperty$Ex(Lit104, Lit107, Lit44, Lit10);
        return C0642runtime.setAndCoerceProperty$Ex(Lit104, Lit31, Lit29, Lit10);
    }

    public Object Clock1$Timer() {
        C0642runtime.setThisForm();
        if (C0642runtime.processAndDelayed$V(new Object[]{lambda$Fn28, lambda$Fn29}) == Boolean.FALSE) {
            return Values.empty;
        }
        C0642runtime.setAndCoerceProperty$Ex(Lit104, Lit5, C0642runtime.callComponentMethod(Lit47, Lit117, LList.list1(C0642runtime.callComponentMethod(Lit47, Lit114, LList.Empty, LList.Empty)), Lit118), Lit14);
        SimpleSymbol simpleSymbol = Lit119;
        SimpleSymbol simpleSymbol2 = Lit120;
        Pair list1 = LList.list1("SSL");
        LList.chain4(LList.chain4(LList.chain4(list1, "smtp.gmail.com", Lit121, "perseusvaldez@gmail.com", "Leo181999"), "Heart Rate Monitor", C0642runtime.callYailPrimitive(C0642runtime.make$Mnyail$Mnlist, LList.list1(C0642runtime.lookupGlobalVarInCurrentFormEnvironment(Lit3, C0642runtime.$Stthe$Mnnull$Mnvalue$St)), Lit122, "make a list"), C0642runtime.callYailPrimitive(C0642runtime.make$Mnyail$Mnlist, LList.Empty, LList.Empty, "make a list"), C0642runtime.callYailPrimitive(C0642runtime.make$Mnyail$Mnlist, LList.Empty, LList.Empty, "make a list")), "Your Heart Rate results are here!", C0642runtime.callYailPrimitive(strings.string$Mnappend, LList.list2(C0642runtime.callYailPrimitive(strings.string$Mnappend, LList.list2("Dear ", C0642runtime.getProperty$1(Lit76, Lit5)), Lit123, "join"), C0642runtime.callYailPrimitive(strings.string$Mnappend, LList.list2("Your Heart Rate results are here! Your measured heart rate is ", C0642runtime.getProperty$1(Lit104, Lit5)), Lit124, "join")), Lit125, "join"), Boolean.FALSE, C0642runtime.callYailPrimitive(C0642runtime.make$Mnyail$Mnlist, LList.Empty, LList.Empty, "make a list"));
        return C0642runtime.callComponentMethod(simpleSymbol, simpleSymbol2, list1, Lit126);
    }

    static Object lambda29() {
        return C0642runtime.getProperty$1(Lit47, Lit91);
    }

    static Object lambda30() {
        return C0642runtime.callYailPrimitive(Scheme.numGrt, LList.list2(C0642runtime.callComponentMethod(Lit47, Lit114, LList.Empty, LList.Empty), Lit115), Lit116, ">");
    }

    /* compiled from: Screen1.yail */
    public class frame extends ModuleBody {
        Screen1 $main;

        public int match1(ModuleMethod moduleMethod, Object obj, CallContext callContext) {
            switch (moduleMethod.selector) {
                case 1:
                    callContext.value1 = obj;
                    callContext.proc = moduleMethod;
                    callContext.f226pc = 1;
                    return 0;
                case 2:
                    if (!(obj instanceof Screen1)) {
                        return -786431;
                    }
                    callContext.value1 = obj;
                    callContext.proc = moduleMethod;
                    callContext.f226pc = 1;
                    return 0;
                case 3:
                    callContext.value1 = obj;
                    callContext.proc = moduleMethod;
                    callContext.f226pc = 1;
                    return 0;
                case 5:
                    if (!(obj instanceof Symbol)) {
                        return -786431;
                    }
                    callContext.value1 = obj;
                    callContext.proc = moduleMethod;
                    callContext.f226pc = 1;
                    return 0;
                case 7:
                    if (!(obj instanceof Symbol)) {
                        return -786431;
                    }
                    callContext.value1 = obj;
                    callContext.proc = moduleMethod;
                    callContext.f226pc = 1;
                    return 0;
                case 12:
                    callContext.value1 = obj;
                    callContext.proc = moduleMethod;
                    callContext.f226pc = 1;
                    return 0;
                case 13:
                    callContext.value1 = obj;
                    callContext.proc = moduleMethod;
                    callContext.f226pc = 1;
                    return 0;
                case 14:
                    if (!(obj instanceof Screen1)) {
                        return -786431;
                    }
                    callContext.value1 = obj;
                    callContext.proc = moduleMethod;
                    callContext.f226pc = 1;
                    return 0;
                default:
                    return super.match1(moduleMethod, obj, callContext);
            }
        }

        public int match2(ModuleMethod moduleMethod, Object obj, Object obj2, CallContext callContext) {
            switch (moduleMethod.selector) {
                case 4:
                    if (!(obj instanceof Symbol)) {
                        return -786431;
                    }
                    callContext.value1 = obj;
                    callContext.value2 = obj2;
                    callContext.proc = moduleMethod;
                    callContext.f226pc = 2;
                    return 0;
                case 5:
                    if (!(obj instanceof Symbol)) {
                        return -786431;
                    }
                    callContext.value1 = obj;
                    callContext.value2 = obj2;
                    callContext.proc = moduleMethod;
                    callContext.f226pc = 2;
                    return 0;
                case 8:
                    if (!(obj instanceof Symbol)) {
                        return -786431;
                    }
                    callContext.value1 = obj;
                    callContext.value2 = obj2;
                    callContext.proc = moduleMethod;
                    callContext.f226pc = 2;
                    return 0;
                case 9:
                    callContext.value1 = obj;
                    callContext.value2 = obj2;
                    callContext.proc = moduleMethod;
                    callContext.f226pc = 2;
                    return 0;
                case 11:
                    callContext.value1 = obj;
                    callContext.value2 = obj2;
                    callContext.proc = moduleMethod;
                    callContext.f226pc = 2;
                    return 0;
                case 17:
                    callContext.value1 = obj;
                    callContext.value2 = obj2;
                    callContext.proc = moduleMethod;
                    callContext.f226pc = 2;
                    return 0;
                default:
                    return super.match2(moduleMethod, obj, obj2, callContext);
            }
        }

        public int match4(ModuleMethod moduleMethod, Object obj, Object obj2, Object obj3, Object obj4, CallContext callContext) {
            switch (moduleMethod.selector) {
                case 10:
                    callContext.value1 = obj;
                    callContext.value2 = obj2;
                    callContext.value3 = obj3;
                    callContext.value4 = obj4;
                    callContext.proc = moduleMethod;
                    callContext.f226pc = 4;
                    return 0;
                case 15:
                    if (!(obj instanceof Screen1)) {
                        return -786431;
                    }
                    callContext.value1 = obj;
                    if (!(obj2 instanceof Component)) {
                        return -786430;
                    }
                    callContext.value2 = obj2;
                    if (!(obj3 instanceof String)) {
                        return -786429;
                    }
                    callContext.value3 = obj3;
                    if (!(obj4 instanceof String)) {
                        return -786428;
                    }
                    callContext.value4 = obj4;
                    callContext.proc = moduleMethod;
                    callContext.f226pc = 4;
                    return 0;
                case 16:
                    if (!(obj instanceof Screen1)) {
                        return -786431;
                    }
                    callContext.value1 = obj;
                    if (!(obj2 instanceof Component)) {
                        return -786430;
                    }
                    callContext.value2 = obj2;
                    if (!(obj3 instanceof String)) {
                        return -786429;
                    }
                    callContext.value3 = obj3;
                    callContext.value4 = obj4;
                    callContext.proc = moduleMethod;
                    callContext.f226pc = 4;
                    return 0;
                default:
                    return super.match4(moduleMethod, obj, obj2, obj3, obj4, callContext);
            }
        }

        public Object apply1(ModuleMethod moduleMethod, Object obj) {
            switch (moduleMethod.selector) {
                case 1:
                    return this.$main.getSimpleName(obj);
                case 2:
                    try {
                        this.$main.onCreate((Bundle) obj);
                        return Values.empty;
                    } catch (ClassCastException e) {
                        throw new WrongType(e, "onCreate", 1, obj);
                    }
                case 3:
                    this.$main.androidLogForm(obj);
                    return Values.empty;
                case 5:
                    try {
                        return this.$main.lookupInFormEnvironment((Symbol) obj);
                    } catch (ClassCastException e2) {
                        throw new WrongType(e2, "lookup-in-form-environment", 1, obj);
                    }
                case 7:
                    try {
                        return this.$main.isBoundInFormEnvironment((Symbol) obj) ? Boolean.TRUE : Boolean.FALSE;
                    } catch (ClassCastException e3) {
                        throw new WrongType(e3, "is-bound-in-form-environment", 1, obj);
                    }
                case 12:
                    this.$main.addToFormDoAfterCreation(obj);
                    return Values.empty;
                case 13:
                    this.$main.sendError(obj);
                    return Values.empty;
                case 14:
                    this.$main.processException(obj);
                    return Values.empty;
                default:
                    return super.apply1(moduleMethod, obj);
            }
        }

        public Object apply4(ModuleMethod moduleMethod, Object obj, Object obj2, Object obj3, Object obj4) {
            boolean z = true;
            switch (moduleMethod.selector) {
                case 10:
                    this.$main.addToComponents(obj, obj2, obj3, obj4);
                    return Values.empty;
                case 15:
                    try {
                        try {
                            try {
                                try {
                                    return this.$main.dispatchEvent((Component) obj, (String) obj2, (String) obj3, (Object[]) obj4) ? Boolean.TRUE : Boolean.FALSE;
                                } catch (ClassCastException e) {
                                    throw new WrongType(e, "dispatchEvent", 4, obj4);
                                }
                            } catch (ClassCastException e2) {
                                throw new WrongType(e2, "dispatchEvent", 3, obj3);
                            }
                        } catch (ClassCastException e3) {
                            throw new WrongType(e3, "dispatchEvent", 2, obj2);
                        }
                    } catch (ClassCastException e4) {
                        throw new WrongType(e4, "dispatchEvent", 1, obj);
                    }
                case 16:
                    Screen1 screen1 = this.$main;
                    try {
                        Component component = (Component) obj;
                        try {
                            String str = (String) obj2;
                            try {
                                if (obj3 == Boolean.FALSE) {
                                    z = false;
                                }
                                try {
                                    screen1.dispatchGenericEvent(component, str, z, (Object[]) obj4);
                                    return Values.empty;
                                } catch (ClassCastException e5) {
                                    throw new WrongType(e5, "dispatchGenericEvent", 4, obj4);
                                }
                            } catch (ClassCastException e6) {
                                throw new WrongType(e6, "dispatchGenericEvent", 3, obj3);
                            }
                        } catch (ClassCastException e7) {
                            throw new WrongType(e7, "dispatchGenericEvent", 2, obj2);
                        }
                    } catch (ClassCastException e8) {
                        throw new WrongType(e8, "dispatchGenericEvent", 1, obj);
                    }
                default:
                    return super.apply4(moduleMethod, obj, obj2, obj3, obj4);
            }
        }

        public Object apply2(ModuleMethod moduleMethod, Object obj, Object obj2) {
            switch (moduleMethod.selector) {
                case 4:
                    try {
                        this.$main.addToFormEnvironment((Symbol) obj, obj2);
                        return Values.empty;
                    } catch (ClassCastException e) {
                        throw new WrongType(e, "add-to-form-environment", 1, obj);
                    }
                case 5:
                    try {
                        return this.$main.lookupInFormEnvironment((Symbol) obj, obj2);
                    } catch (ClassCastException e2) {
                        throw new WrongType(e2, "lookup-in-form-environment", 1, obj);
                    }
                case 8:
                    try {
                        this.$main.addToGlobalVarEnvironment((Symbol) obj, obj2);
                        return Values.empty;
                    } catch (ClassCastException e3) {
                        throw new WrongType(e3, "add-to-global-var-environment", 1, obj);
                    }
                case 9:
                    this.$main.addToEvents(obj, obj2);
                    return Values.empty;
                case 11:
                    this.$main.addToGlobalVars(obj, obj2);
                    return Values.empty;
                case 17:
                    return this.$main.lookupHandler(obj, obj2);
                default:
                    return super.apply2(moduleMethod, obj, obj2);
            }
        }

        public Object apply0(ModuleMethod moduleMethod) {
            switch (moduleMethod.selector) {
                case 18:
                    return Screen1.lambda2();
                case 19:
                    this.$main.$define();
                    return Values.empty;
                case 20:
                    return Screen1.lambda3();
                case 21:
                    return Screen1.lambda4();
                case 22:
                    return Screen1.lambda5();
                case 23:
                    return Screen1.lambda6();
                case 24:
                    return Screen1.lambda7();
                case 25:
                    return Screen1.lambda8();
                case 26:
                    return Screen1.lambda9();
                case 27:
                    return Screen1.lambda10();
                case 28:
                    return this.$main.ListPicker1$BeforePicking();
                case 29:
                    return this.$main.ListPicker1$AfterPicking();
                case 30:
                    return Screen1.lambda11();
                case 31:
                    return Screen1.lambda12();
                case 32:
                    return this.$main.disconnectBT$Click();
                case 33:
                    return Screen1.lambda13();
                case 34:
                    return Screen1.lambda14();
                case 35:
                    return Screen1.lambda15();
                case 36:
                    return Screen1.lambda16();
                case 37:
                    return Screen1.lambda17();
                case 38:
                    return Screen1.lambda18();
                case 39:
                    return Screen1.lambda19();
                case 40:
                    return Screen1.lambda20();
                case 41:
                    return Screen1.lambda21();
                case 42:
                    return Screen1.lambda22();
                case 43:
                    return this.$main.startBT$Click();
                case 44:
                    return Screen1.lambda23();
                case 45:
                    return Screen1.lambda24();
                case 46:
                    return Screen1.lambda25();
                case 47:
                    return Screen1.lambda26();
                case 48:
                    return Screen1.lambda27();
                case 49:
                    return Screen1.lambda28();
                case 50:
                    return Screen1.lambda29();
                case 51:
                    return Screen1.lambda30();
                case 52:
                    return this.$main.Clock1$Timer();
                default:
                    return super.apply0(moduleMethod);
            }
        }

        public int match0(ModuleMethod moduleMethod, CallContext callContext) {
            switch (moduleMethod.selector) {
                case 18:
                    callContext.proc = moduleMethod;
                    callContext.f226pc = 0;
                    return 0;
                case 19:
                    callContext.proc = moduleMethod;
                    callContext.f226pc = 0;
                    return 0;
                case 20:
                    callContext.proc = moduleMethod;
                    callContext.f226pc = 0;
                    return 0;
                case 21:
                    callContext.proc = moduleMethod;
                    callContext.f226pc = 0;
                    return 0;
                case 22:
                    callContext.proc = moduleMethod;
                    callContext.f226pc = 0;
                    return 0;
                case 23:
                    callContext.proc = moduleMethod;
                    callContext.f226pc = 0;
                    return 0;
                case 24:
                    callContext.proc = moduleMethod;
                    callContext.f226pc = 0;
                    return 0;
                case 25:
                    callContext.proc = moduleMethod;
                    callContext.f226pc = 0;
                    return 0;
                case 26:
                    callContext.proc = moduleMethod;
                    callContext.f226pc = 0;
                    return 0;
                case 27:
                    callContext.proc = moduleMethod;
                    callContext.f226pc = 0;
                    return 0;
                case 28:
                    callContext.proc = moduleMethod;
                    callContext.f226pc = 0;
                    return 0;
                case 29:
                    callContext.proc = moduleMethod;
                    callContext.f226pc = 0;
                    return 0;
                case 30:
                    callContext.proc = moduleMethod;
                    callContext.f226pc = 0;
                    return 0;
                case 31:
                    callContext.proc = moduleMethod;
                    callContext.f226pc = 0;
                    return 0;
                case 32:
                    callContext.proc = moduleMethod;
                    callContext.f226pc = 0;
                    return 0;
                case 33:
                    callContext.proc = moduleMethod;
                    callContext.f226pc = 0;
                    return 0;
                case 34:
                    callContext.proc = moduleMethod;
                    callContext.f226pc = 0;
                    return 0;
                case 35:
                    callContext.proc = moduleMethod;
                    callContext.f226pc = 0;
                    return 0;
                case 36:
                    callContext.proc = moduleMethod;
                    callContext.f226pc = 0;
                    return 0;
                case 37:
                    callContext.proc = moduleMethod;
                    callContext.f226pc = 0;
                    return 0;
                case 38:
                    callContext.proc = moduleMethod;
                    callContext.f226pc = 0;
                    return 0;
                case 39:
                    callContext.proc = moduleMethod;
                    callContext.f226pc = 0;
                    return 0;
                case 40:
                    callContext.proc = moduleMethod;
                    callContext.f226pc = 0;
                    return 0;
                case 41:
                    callContext.proc = moduleMethod;
                    callContext.f226pc = 0;
                    return 0;
                case 42:
                    callContext.proc = moduleMethod;
                    callContext.f226pc = 0;
                    return 0;
                case 43:
                    callContext.proc = moduleMethod;
                    callContext.f226pc = 0;
                    return 0;
                case 44:
                    callContext.proc = moduleMethod;
                    callContext.f226pc = 0;
                    return 0;
                case 45:
                    callContext.proc = moduleMethod;
                    callContext.f226pc = 0;
                    return 0;
                case 46:
                    callContext.proc = moduleMethod;
                    callContext.f226pc = 0;
                    return 0;
                case 47:
                    callContext.proc = moduleMethod;
                    callContext.f226pc = 0;
                    return 0;
                case 48:
                    callContext.proc = moduleMethod;
                    callContext.f226pc = 0;
                    return 0;
                case 49:
                    callContext.proc = moduleMethod;
                    callContext.f226pc = 0;
                    return 0;
                case 50:
                    callContext.proc = moduleMethod;
                    callContext.f226pc = 0;
                    return 0;
                case 51:
                    callContext.proc = moduleMethod;
                    callContext.f226pc = 0;
                    return 0;
                case 52:
                    callContext.proc = moduleMethod;
                    callContext.f226pc = 0;
                    return 0;
                default:
                    return super.match0(moduleMethod, callContext);
            }
        }
    }

    public String getSimpleName(Object object) {
        return object.getClass().getSimpleName();
    }

    public void onCreate(Bundle icicle) {
        AppInventorCompatActivity.setClassicModeFromYail(false);
        super.onCreate(icicle);
    }

    public void androidLogForm(Object message) {
    }

    public void addToFormEnvironment(Symbol name, Object object) {
        androidLogForm(Format.formatToString(0, "Adding ~A to env ~A with value ~A", name, this.form$Mnenvironment, object));
        this.form$Mnenvironment.put(name, object);
    }

    public Object lookupInFormEnvironment(Symbol name, Object default$Mnvalue) {
        boolean x = ((this.form$Mnenvironment == null ? 1 : 0) + 1) & true;
        if (x) {
            if (!this.form$Mnenvironment.isBound(name)) {
                return default$Mnvalue;
            }
        } else if (!x) {
            return default$Mnvalue;
        }
        return this.form$Mnenvironment.get(name);
    }

    public boolean isBoundInFormEnvironment(Symbol name) {
        return this.form$Mnenvironment.isBound(name);
    }

    public void addToGlobalVarEnvironment(Symbol name, Object object) {
        androidLogForm(Format.formatToString(0, "Adding ~A to env ~A with value ~A", name, this.global$Mnvar$Mnenvironment, object));
        this.global$Mnvar$Mnenvironment.put(name, object);
    }

    public void addToEvents(Object component$Mnname, Object event$Mnname) {
        this.events$Mnto$Mnregister = C0654lists.cons(C0654lists.cons(component$Mnname, event$Mnname), this.events$Mnto$Mnregister);
    }

    public void addToComponents(Object container$Mnname, Object component$Mntype, Object component$Mnname, Object init$Mnthunk) {
        this.components$Mnto$Mncreate = C0654lists.cons(LList.list4(container$Mnname, component$Mntype, component$Mnname, init$Mnthunk), this.components$Mnto$Mncreate);
    }

    public void addToGlobalVars(Object var, Object val$Mnthunk) {
        this.global$Mnvars$Mnto$Mncreate = C0654lists.cons(LList.list2(var, val$Mnthunk), this.global$Mnvars$Mnto$Mncreate);
    }

    public void addToFormDoAfterCreation(Object thunk) {
        this.form$Mndo$Mnafter$Mncreation = C0654lists.cons(thunk, this.form$Mndo$Mnafter$Mncreation);
    }

    public void sendError(Object error) {
        RetValManager.sendError(error == null ? null : error.toString());
    }

    public void processException(Object ex) {
        Object apply1 = Scheme.applyToArgs.apply1(GetNamedPart.getNamedPart.apply2(ex, Lit1));
        RuntimeErrorAlert.alert(this, apply1 == null ? null : apply1.toString(), ex instanceof YailRuntimeError ? ((YailRuntimeError) ex).getErrorType() : "Runtime Error", "End Application");
    }

    public boolean dispatchEvent(Component componentObject, String registeredComponentName, String eventName, Object[] args) {
        boolean x;
        SimpleSymbol registeredObject = misc.string$To$Symbol(registeredComponentName);
        if (!isBoundInFormEnvironment(registeredObject)) {
            EventDispatcher.unregisterEventForDelegation(this, registeredComponentName, eventName);
            return false;
        } else if (lookupInFormEnvironment(registeredObject) != componentObject) {
            return false;
        } else {
            try {
                Scheme.apply.apply2(lookupHandler(registeredComponentName, eventName), LList.makeList(args, 0));
                return true;
            } catch (StopBlocksExecution e) {
                return false;
            } catch (PermissionException exception) {
                exception.printStackTrace();
                if (this == componentObject) {
                    x = true;
                } else {
                    x = false;
                }
                if (!x ? x : IsEqual.apply(eventName, "PermissionNeeded")) {
                    processException(exception);
                } else {
                    PermissionDenied(componentObject, eventName, exception.getPermissionNeeded());
                }
                return false;
            } catch (Throwable exception2) {
                androidLogForm(exception2.getMessage());
                exception2.printStackTrace();
                processException(exception2);
                return false;
            }
        }
    }

    public void dispatchGenericEvent(Component componentObject, String eventName, boolean notAlreadyHandled, Object[] args) {
        Boolean bool;
        boolean x = true;
        Object handler = lookupInFormEnvironment(misc.string$To$Symbol(strings.stringAppend("any$", getSimpleName(componentObject), "$", eventName)));
        if (handler != Boolean.FALSE) {
            try {
                Apply apply = Scheme.apply;
                if (notAlreadyHandled) {
                    bool = Boolean.TRUE;
                } else {
                    bool = Boolean.FALSE;
                }
                apply.apply2(handler, C0654lists.cons(componentObject, C0654lists.cons(bool, LList.makeList(args, 0))));
            } catch (StopBlocksExecution e) {
            } catch (PermissionException exception) {
                exception.printStackTrace();
                if (this != componentObject) {
                    x = false;
                }
                if (!x ? x : IsEqual.apply(eventName, "PermissionNeeded")) {
                    processException(exception);
                } else {
                    PermissionDenied(componentObject, eventName, exception.getPermissionNeeded());
                }
            } catch (Throwable exception2) {
                androidLogForm(exception2.getMessage());
                exception2.printStackTrace();
                processException(exception2);
            }
        }
    }

    public Object lookupHandler(Object componentName, Object eventName) {
        String str = null;
        String obj = componentName == null ? null : componentName.toString();
        if (eventName != null) {
            str = eventName.toString();
        }
        return lookupInFormEnvironment(misc.string$To$Symbol(EventDispatcher.makeFullEventName(obj, str)));
    }

    public void $define() {
        Object reverse;
        Object obj;
        Object reverse2;
        Object obj2;
        Object obj3;
        Object var;
        Object component$Mnname;
        Object obj4;
        Language.setDefaults(Scheme.getInstance());
        try {
            run();
        } catch (Exception exception) {
            androidLogForm(exception.getMessage());
            processException(exception);
        }
        Screen1 = this;
        addToFormEnvironment(Lit0, this);
        Object obj5 = this.events$Mnto$Mnregister;
        while (obj5 != LList.Empty) {
            try {
                Pair arg0 = (Pair) obj5;
                Object event$Mninfo = arg0.getCar();
                Object apply1 = C0654lists.car.apply1(event$Mninfo);
                String obj6 = apply1 == null ? null : apply1.toString();
                Object apply12 = C0654lists.cdr.apply1(event$Mninfo);
                EventDispatcher.registerEventForDelegation(this, obj6, apply12 == null ? null : apply12.toString());
                obj5 = arg0.getCdr();
            } catch (ClassCastException e) {
                throw new WrongType(e, "arg0", -2, obj5);
            }
        }
        try {
            LList components = C0654lists.reverse(this.components$Mnto$Mncreate);
            addToGlobalVars(Lit2, lambda$Fn1);
            reverse = C0654lists.reverse(this.form$Mndo$Mnafter$Mncreation);
            while (reverse != LList.Empty) {
                Pair arg02 = (Pair) reverse;
                misc.force(arg02.getCar());
                reverse = arg02.getCdr();
            }
            obj = components;
            while (obj != LList.Empty) {
                Pair arg03 = (Pair) obj;
                Object component$Mninfo = arg03.getCar();
                component$Mnname = C0654lists.caddr.apply1(component$Mninfo);
                C0654lists.cadddr.apply1(component$Mninfo);
                Object component$Mnobject = Invoke.make.apply2(C0654lists.cadr.apply1(component$Mninfo), lookupInFormEnvironment((Symbol) C0654lists.car.apply1(component$Mninfo)));
                SlotSet.set$Mnfield$Ex.apply3(this, component$Mnname, component$Mnobject);
                addToFormEnvironment((Symbol) component$Mnname, component$Mnobject);
                obj = arg03.getCdr();
            }
            reverse2 = C0654lists.reverse(this.global$Mnvars$Mnto$Mncreate);
            while (reverse2 != LList.Empty) {
                Pair arg04 = (Pair) reverse2;
                Object var$Mnval = arg04.getCar();
                var = C0654lists.car.apply1(var$Mnval);
                addToGlobalVarEnvironment((Symbol) var, Scheme.applyToArgs.apply1(C0654lists.cadr.apply1(var$Mnval)));
                reverse2 = arg04.getCdr();
            }
            LList component$Mndescriptors = components;
            obj2 = component$Mndescriptors;
            while (obj2 != LList.Empty) {
                Pair arg05 = (Pair) obj2;
                Object component$Mninfo2 = arg05.getCar();
                C0654lists.caddr.apply1(component$Mninfo2);
                Object init$Mnthunk = C0654lists.cadddr.apply1(component$Mninfo2);
                if (init$Mnthunk != Boolean.FALSE) {
                    Scheme.applyToArgs.apply1(init$Mnthunk);
                }
                obj2 = arg05.getCdr();
            }
            obj3 = component$Mndescriptors;
            while (obj3 != LList.Empty) {
                Pair arg06 = (Pair) obj3;
                Object component$Mninfo3 = arg06.getCar();
                Object component$Mnname2 = C0654lists.caddr.apply1(component$Mninfo3);
                C0654lists.cadddr.apply1(component$Mninfo3);
                callInitialize(SlotGet.field.apply2(this, component$Mnname2));
                obj3 = arg06.getCdr();
            }
        } catch (ClassCastException e2) {
            throw new WrongType(e2, "arg0", -2, obj3);
        } catch (ClassCastException e3) {
            throw new WrongType(e3, "arg0", -2, obj2);
        } catch (ClassCastException e4) {
            throw new WrongType(e4, "add-to-global-var-environment", 0, var);
        } catch (ClassCastException e5) {
            throw new WrongType(e5, "arg0", -2, reverse2);
        } catch (ClassCastException e6) {
            throw new WrongType(e6, "add-to-form-environment", 0, component$Mnname);
        } catch (ClassCastException e7) {
            throw new WrongType(e7, "lookup-in-form-environment", 0, obj4);
        } catch (ClassCastException e8) {
            throw new WrongType(e8, "arg0", -2, obj);
        } catch (ClassCastException e9) {
            throw new WrongType(e9, "arg0", -2, reverse);
        } catch (YailRuntimeError exception2) {
            processException(exception2);
        }
    }

    public static SimpleSymbol lambda1symbolAppend$V(Object[] argsArray) {
        LList symbols = LList.makeList(argsArray, 0);
        Apply apply = Scheme.apply;
        ModuleMethod moduleMethod = strings.string$Mnappend;
        Object obj = LList.Empty;
        LList lList = symbols;
        while (lList != LList.Empty) {
            try {
                Pair arg0 = (Pair) lList;
                Object arg02 = arg0.getCdr();
                Object car = arg0.getCar();
                try {
                    obj = Pair.make(misc.symbol$To$String((Symbol) car), obj);
                    lList = arg02;
                } catch (ClassCastException e) {
                    throw new WrongType(e, "symbol->string", 1, car);
                }
            } catch (ClassCastException e2) {
                throw new WrongType(e2, "arg0", -2, lList);
            }
        }
        Object apply2 = apply.apply2(moduleMethod, LList.reverseInPlace(obj));
        try {
            return misc.string$To$Symbol((CharSequence) apply2);
        } catch (ClassCastException e3) {
            throw new WrongType(e3, "string->symbol", 1, apply2);
        }
    }

    static Object lambda2() {
        return null;
    }
}
