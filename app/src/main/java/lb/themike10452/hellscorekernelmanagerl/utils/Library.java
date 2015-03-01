package lb.themike10452.hellscorekernelmanagerl.utils;

/**
 * Created by Mike on 2/22/2015.
 */
public class Library {

    public static String CPU_SYSFS = "/sys/devices/system/cpu";
    public static String BATTERY_SYSFS = "/sys/class/power_supply/battery";
    public static String CPU_TEMP_PATH = "/sys/class/thermal/thermal_zone7/temp";
    public static String GOV0 = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor";
    public static String GOV1 = "/sys/devices/system/cpu/cpu1/cpufreq/scaling_governor";
    public static String GOV2 = "/sys/devices/system/cpu/cpu2/cpufreq/scaling_governor";
    public static String GOV3 = "/sys/devices/system/cpu/cpu3/cpufreq/scaling_governor";
    public static String MAX_FREQ_PATH = "/sys/devices/system/cpu/cpu%s/cpufreq/scaling_max_freq";
    public static String MIN_FREQ_PATH = "/sys/devices/system/cpu/cpu%s/cpufreq/scaling_min_freq";
    public static String MAX_CPUS_ONLINE_PATH0 = "/sys/module/msm_hotplug/max_cpus_online";
    public static String MAX_CPUS_ONLINE_PATH1 = "/sys/kernel/msm_mpdecision/conf/max_cpus";
    public static String MIN_CPUS_ONLINE_PATH0 = "/sys/module/msm_hotplug/min_cpus_online";
    public static String MIN_CPUS_ONLINE_PATH1 = "/sys/kernel/msm_mpdecision/conf/min_cpus";
    public static String BOOSTED_CPUS_PATH = "/sys/module/msm_hotplug/cpus_boosted";
    public static String TOUCH_BOOST_PATH = "/sys/kernel/msm_mpdecision/conf/boost_enabled";
    public static String TOUCH_BOOST_FREQS_PATH = "/sys/kernel/msm_mpdecision/conf/boost_freqs";
    public static String SUSPEND_FREQ_PATH = "/sys/module/msm_hotplug/suspend_freq";
    public static String BOOST_LOCK_DURATION_PATH = "/sys/module/msm_hotplug/boost_lock_duration";
    public static String SCREEN_OFF_SINGLE_CORE_PATH = "/sys/kernel/msm_mpdecision/conf/scroff_single_core";
    public static String SCREEN_OFF_MAX_FREQ = "/sys/devices/system/cpu/cpu0/cpufreq/screen_off_max_freq";
    public static String SCREEN_OFF_MAX_STATE = "/sys/devices/system/cpu/cpu0/cpufreq/screen_off_max";
    public static String AVAIL_FREQ_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies";
    public static String AVAIL_GOV_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_governors";
    public static String CPU_GOV_CFG_PATH = "/sys/devices/system/cpu/cpufreq/%s/";
    public static String CPU_IDLE_C0_PATH = "/sys/module/pm_8x60/modes/cpu%s/wfi/idle_enabled";
    public static String CPU_IDLE_C1_PATH = "/sys/module/pm_8x60/modes/cpu%s/retention/idle_enabled";
    public static String CPU_IDLE_C2_PATH = "/sys/module/pm_8x60/modes/cpu%s/standalone_power_collapse/idle_enabled";
    public static String CPU_IDLE_C3_PATH = "/sys/module/pm_8x60/modes/cpu%s/power_collapse/idle_enabled";
    public static String kgamma_r = "/sys/devices/platform/mipi_lgit.1537/kgamma_r";
    public static String kgamma_g = "/sys/devices/platform/mipi_lgit.1537/kgamma_g";
    public static String kgamma_b = "/sys/devices/platform/mipi_lgit.1537/kgamma_b";
    public static String kgamma_red = "/sys/devices/platform/mipi_lgit.1537/kgamma_red";
    public static String kgamma_green = "/sys/devices/platform/mipi_lgit.1537/kgamma_green";
    public static String kgamma_blue = "/sys/devices/platform/mipi_lgit.1537/kgamma_blue";
    public static String kcal = "/sys/devices/platform/kcal_ctrl.0/kcal";
    public static String kgamma_apply = "/sys/devices/platform/mipi_lgit.1537/kgamma_apply";
    public static String BRIGHTNESS_MODE_PATH = "/sys/devices/i2c-0/0-0038/lm3530_br_mode";
    public static String DT2W_PATH = "/sys/android_touch/doubletap2wake";
    public static String S2W_PATH = "/sys/android_touch/sweep2wake";
    public static String DT2W_PATH_S = "/sys/devices/virtual/input/lge_touch/dt_wake_enabled";
    public static String TOUCHWAKE_S = "/sys/devices/virtual/misc/touchwake/enabled";
    public static String TOUCHWAKE_DELAY = "/sys/devices/virtual/misc/touchwake/delay";
    public static String IO_SCHED_PATH = "/sys/block/mmcblk0/queue/scheduler";
    public static String READ_AHEAD_BUFFER_PATH = "/sys/block/mmcblk0/queue/read_ahead_kb";
    public static String DYN_FSYNC_PATH = "/sys/kernel/dyn_fsync/Dyn_fsync_active";
    public static String FASTCHARGE_PATH = "/sys/kernel/fast_charge/force_fast_charge";
    public static String MSM_THERMAL_PATH = "/sys/module/msm_thermal/parameters/temp_max";
    public static String MSM_THERMAL_PATH_HAMMERHEAD = "/sys/module/msm_thermal/parameters/limit_temp";
    public static String NET_TCP_ALLOWED = "/proc/sys/net/ipv4/tcp_allowed_congestion_control";
    public static String NET_TCP_AVAILABLE = "/proc/sys/net/ipv4/tcp_available_congestion_control";
    public static String NET_TCP_CONGST = "/proc/sys/net/ipv4/tcp_congestion_control";
    public static String VDD_LEVELS = "/sys/devices/system/cpu/cpufreq/vdd_table/vdd_levels";
    public static String UV_MV_TABLE = "/sys/devices/system/cpu/cpu0/cpufreq/UV_mV_table";
    public static String CPU_TIME_IN_STATE = "/sys/devices/system/cpu/cpu0/cpufreq/stats/time_in_state";
    public static String VIBRATOR_AMP = "/sys/class/timed_output/vibrator/amp";
    public static String GPU_AVAIL_FREQ_PATH = "/sys/devices/platform/kgsl-3d0.0/kgsl/kgsl-3d0/gpu_available_frequencies";
    public static String GPU_AVAIL_FREQ_PATH_HAMMERHEAD = "/sys/devices/fdb00000.qcom,kgsl-3d0/kgsl/kgsl-3d0/gpu_available_frequencies";
    public static String GPU_MAX_CLK_PATH = "/sys/devices/platform/kgsl-3d0.0/kgsl/kgsl-3d0/max_gpuclk";
    public static String GPU_MAX_CLK_PATH_HAMMERHEAD = "/sys/devices/fdb00000.qcom,kgsl-3d0/kgsl/kgsl-3d0/max_gpuclk";
    public static String GPU_POLICY_PATH = "/sys/devices/platform/kgsl-3d0.0/kgsl/kgsl-3d0/pwrscale/policy";
    public static String GPU_POLICY_PATH_HAMMERHEAD = "/sys/devices/fdb00000.qcom,kgsl-3d0/kgsl/kgsl-3d0/pwrscale/policy";
    public static String GPU_GOV_PATH = "/sys/devices/platform/kgsl-3d0.0/kgsl/kgsl-3d0/pwrscale/trustzone/governor";
    public static String GPU_GOV_PATH_HAMMERHEAD = "/sys/devices/fdb00000.qcom,kgsl-3d0/kgsl/kgsl-3d0/pwrscale/trustzone/governor";
    public static String HP_GAIN_PATH = "/sys/kernel/sound_control_3/gpl_headphone_gain";
    public static String HP_PA_GAIN_PATH = "/sys/kernel/sound_control_3/gpl_headphone_pa_gain";
    public static String SPEAKER_GAIN_PATH = "/sys/kernel/sound_control_3/gpl_speaker_gain";
    public static String MIC_GAIN_PATH = "/sys/kernel/sound_control_3/gpl_mic_gain";
    public static String CAMMIC_GAIN_PATH = "/sys/kernel/sound_control_3/gpl_cam_mic_gain";
    public static String SOUND_LOCK_PATH = "/sys/kernel/sound_control_3/gpl_sound_control_locked";
    public static String SOUND_CONTROL_VERSION_PATH = "/sys/kernel/sound_control_3/gpl_sound_control_version";

    public static String TAG_LOADED_COLOR_PROFILE = "loaded_color_profile";
    public static String report_file = "HKM_REPORT.TXT";
    public static String busybox_onRails = "https://play.google.com/store/apps/details?id=me.timos.busyboxonrails";
    public static String my_email = "thephenommvp@gmail.com";
    public static String setOnBootAgentFile = "/system/etc/init.d/99hellscore_kernel_settings";
    public static String kernel_thread = "http://forum.xda-developers.com/showthread.php?t=2495373";
    public static String app_thread = "http://forum.xda-developers.com/showthread.php?t=2669442";
    public static String googlePlus_community = "https://plus.google.com/communities/115269166678032690242";

    public static String[] gpu_governors = new String[]{"conservative", "ondemand", "performance"};
    public static String[] gpu_governors_new = new String[]{/*"interactive",*/ "ondemand", "performance"};

    /*public static ArrayList<String> getColorProfiles(Context context) {
        ArrayList<String> arrayList = new ArrayList<String>();
        arrayList.add("~CUSTOM~");
        arrayList.addAll(MySQLiteAdapter.selectColumn(context, DBHelper.COLOR_PROFILES_TABLE, DBHelper.COLOR_PROFILES_TABLE_KEY));
        return arrayList;
    }*/

}
