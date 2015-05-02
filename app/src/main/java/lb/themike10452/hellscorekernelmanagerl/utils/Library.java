package lb.themike10452.hellscorekernelmanagerl.utils;

/**
 * Created by Mike on 2/22/2015.
 */
public class Library {
    /** Monitoring **/
    public static final String MON_CPU_SYSFS = "/sys/devices/system/cpu";
    public static final String MON_BATTERY_SYSFS = "/sys/class/power_supply/battery";
    public static final String MON_CPU_TEMP = "/sys/class/thermal/thermal_zone7/temp";
    public static final String MON_CPU_TIME_IN_STATE = "/sys/devices/system/cpu/cpu0/cpufreq/stats/time_in_state";

    /** CPU **/
    public static final String CPU_GOVERNOR = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor";
    public static final String CPU_MAX_FREQ = "/sys/devices/system/cpu/cpu%s/cpufreq/scaling_max_freq";
    public static final String CPU_MIN_FREQ = "/sys/devices/system/cpu/cpu%s/cpufreq/scaling_min_freq";
    public static final String CPU_MSM_HOTPLUG_ENABLED = "/sys/module/msm_hotplug/msm_enabled";
    public static final String CPU_MSM_MPDECISION_ENABLED = "/sys/module/msm_mpdecision/parameters/enabled";
    public static final String CPU_MAX_CORES_ONLINE_1 = "/sys/module/msm_hotplug/max_cpus_online";
    public static final String CPU_MAX_CORES_ONLINE_2 = "/sys/kernel/msm_mpdecision/conf/max_cpus";
    public static final String CPU_MIN_CORES_ONLINE_1 = "/sys/module/msm_hotplug/min_cpus_online";
    public static final String CPU_MIN_CORES_ONLINE_0 = "/sys/kernel/msm_mpdecision/conf/min_cpus";
    public static final String CPU_MAX_CORES_SUSP = "/sys/module/msm_hotplug/max_cpus_online_susp";
    public static final String CPU_BOOSTED_CORES = "/sys/module/msm_hotplug/cpus_boosted";
    public static final String CPU_TOUCH_BOOST = "/sys/kernel/msm_mpdecision/conf/boost_enabled";
    public static final String CPU_TOUCH_BOOST_FREQS = "/sys/kernel/msm_mpdecision/conf/boost_freqs";
    public static final String CPU_BOOST_LOCK_DURATION = "/sys/module/msm_hotplug/boost_lock_duration";
    public static final String CPU_SCREEN_OFF_SINGLE_CORE = "/sys/kernel/msm_mpdecision/conf/scroff_single_core";
    public static final String CPU_SCREEN_OFF_MAX = "/sys/devices/system/cpu/cpu0/cpufreq/screen_off_max_freq";
    public static final String CPU_SCREEN_OFF_MAX_STATE = "/sys/devices/system/cpu/cpu0/cpufreq/screen_off_max";
    public static final String CPU_AVAIL_FREQS = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies";
    public static final String CPU_AVAIL_GOVS = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_governors";
    public static final String CPU_GOV_CFG_PATH = "/sys/devices/system/cpu/cpufreq/%s/";
    public static final String CPU_IDLE_C0 = "/sys/module/pm_8x60/modes/cpu%s/wfi/idle_enabled";
    public static final String CPU_IDLE_C1 = "/sys/module/pm_8x60/modes/cpu%s/retention/idle_enabled";
    public static final String CPU_IDLE_C2 = "/sys/module/pm_8x60/modes/cpu%s/standalone_power_collapse/idle_enabled";
    public static final String CPU_IDLE_C3 = "/sys/module/pm_8x60/modes/cpu%s/power_collapse/idle_enabled";
    public static final String CPU_VDD_LEVELS = "/sys/devices/system/cpu/cpufreq/vdd_table/vdd_levels";
    public static final String CPU_UV_MV_TABLE = "/sys/devices/system/cpu/cpu0/cpufreq/UV_mV_table";
    //public static final String CPU_SUSPEND_FREQ = "/sys/module/msm_hotplug/suspend_freq";

    /** LCD **/
    public static final String LCD_KGAMMA_R = "/sys/devices/platform/mipi_lgit.1537/kgamma_r";
    public static final String LCD_KGAMMA_G = "/sys/devices/platform/mipi_lgit.1537/kgamma_g";
    public static final String LCD_KGAMMA_B = "/sys/devices/platform/mipi_lgit.1537/kgamma_b";
    public static final String LCD_KGAMMA_RED = "/sys/devices/platform/mipi_lgit.1537/kgamma_red";
    public static final String LCD_KGAMMA_GREEN = "/sys/devices/platform/mipi_lgit.1537/kgamma_green";
    public static final String LCD_KGAMMA_BLUE = "/sys/devices/platform/mipi_lgit.1537/kgamma_blue";
    public static final String LCD_KGAMMA_KCAL = "/sys/devices/platform/kcal_ctrl.0/kcal";
    public static final String LCD_KGAMMA_APPLY = "/sys/devices/platform/mipi_lgit.1537/kgamma_apply";
    public static final String LCD_BRIGHTNESS_MODE = "/sys/devices/i2c-0/0-0038/lm3530_br_mode";
    public static final String LCD_MAX_BRIGHTNESS = "/sys/devices/i2c-0/0-0038/lm3530_max_br";
    public static final String LCD_MIN_BRIGHTNESS = "/sys/devices/i2c-0/0-0038/lm3530_min_br";

    /** TOUCH WAKE **/
    public static final String WAKE_DT2W_1 = "/sys/android_touch/doubletap2wake";
    public static final String WAKE_DT2W_2 = "/sys/devices/virtual/input/lge_touch/dt_wake_enabled";
    public static final String WAKE_S2W_PATH = "/sys/android_touch/sweep2wake";
    public static final String WAKE_S2D_PATH = "/sys/android_touch/sweep2dim";
    public static final String WAKE_TOUCHWAKE = "/sys/devices/virtual/misc/touchwake/enabled";
    public static final String WAKE_TOUCHWAKE_DELAY = "/sys/devices/virtual/misc/touchwake/delay";
    public static final String WAKE_DT2W_FEATHER = "/sys/android_touch/doubletap2wake_feather";

    /** MISC **/
    public static final String MISC_IO_SCHED = "/sys/block/mmcblk0/queue/scheduler";
    public static final String MISC_READ_AHEAD_BUFFER = "/sys/block/mmcblk0/queue/read_ahead_kb";
    public static final String MISC_DYN_FSYNC = "/sys/kernel/dyn_fsync/Dyn_fsync_active";
    public static final String MISC_FASTCHARGE = "/sys/kernel/fast_charge/force_fast_charge";
    public static final String MISC_MSM_THERMAL_1 = "/sys/module/msm_thermal/parameters/temp_max";
    public static final String MISC_MSM_THERMAL_2 = "/sys/module/msm_thermal/parameters/limit_temp";
    public static final String MISC_NET_TCP_ALLOWED = "/proc/sys/net/ipv4/tcp_allowed_congestion_control";
    public static final String MISC_NET_TCP_AVAILABLE = "/proc/sys/net/ipv4/tcp_available_congestion_control";
    public static final String MISC_NET_TCP_CONGST = "/proc/sys/net/ipv4/tcp_congestion_control";
    public static final String MISC_VIBRATOR_AMP = "/sys/class/timed_output/vibrator/amp";
    public static final String MISC_BLX_LIMIT = "/sys/devices/virtual/misc/batterylifeextender/charging_limit";

    /** GPU **/
    public static final String GPU_AVAIL_FREQS_1 = "/sys/devices/platform/kgsl-3d0.0/kgsl/kgsl-3d0/gpu_available_frequencies";
    public static final String GPU_AVAIL_FREQ2_2 = "/sys/devices/fdb00000.qcom,kgsl-3d0/kgsl/kgsl-3d0/gpu_available_frequencies";
    public static final String GPU_MAX_FREQ_1 = "/sys/devices/platform/kgsl-3d0.0/kgsl/kgsl-3d0/max_gpuclk";
    public static final String GPU_MAX_FREQ_2 = "/sys/devices/fdb00000.qcom,kgsl-3d0/kgsl/kgsl-3d0/max_gpuclk";
    public static final String GPU_POLICY_1 = "/sys/devices/platform/kgsl-3d0.0/kgsl/kgsl-3d0/pwrscale/policy";
    public static final String GPU_POLICY_2 = "/sys/devices/fdb00000.qcom,kgsl-3d0/kgsl/kgsl-3d0/pwrscale/policy";
    public static final String GPU_AVAILABLE_POLICIES = "/sys/devices/platform/kgsl-3d0.0/kgsl/kgsl-3d0/pwrscale/avail_policies";
    public static final String GPU_GOVERNOR_1 = "/sys/devices/platform/kgsl-3d0.0/kgsl/kgsl-3d0/pwrscale/trustzone/governor";
    public static final String GPU_GOVERNOR_2 = "/sys/devices/fdb00000.qcom,kgsl-3d0/kgsl/kgsl-3d0/pwrscale/trustzone/governor";
    public static final String GPU_MV_TABLE = "/sys/devices/system/cpu/cpu0/cpufreq/gpu_mv_table";

    /** SOUND **/
    public static final String SOUND_HP_GAIN = "/sys/kernel/sound_control_3/gpl_headphone_gain";
    public static final String SOUND_HP_PA_GAIN = "/sys/kernel/sound_control_3/gpl_headphone_pa_gain";
    public static final String SOUND_SPEAKER_GAIN = "/sys/kernel/sound_control_3/gpl_speaker_gain";
    public static final String SOUND_MIC_GAIN = "/sys/kernel/sound_control_3/gpl_mic_gain";
    public static final String SOUND_CAMMIC_GAIN = "/sys/kernel/sound_control_3/gpl_cam_mic_gain";

    public static String kernel_thread = "http://forum.xda-developers.com/showthread.php?t=2495373";
    public static String app_thread = "http://forum.xda-developers.com/showthread.php?t=2669442";
    public static String googlePlus_community = "https://plus.google.com/communities/115269166678032690242";
}
