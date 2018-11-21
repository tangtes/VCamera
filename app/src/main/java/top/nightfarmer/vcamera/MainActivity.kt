package top.nightfarmer.vcamera

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File


class MainActivity : AppCompatActivity() {

    private var mUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        to_take_pic.setOnClickListener {
            val openCameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE) //系统常量， 启动相机的关键

            if (hasPreferredApplication(this@MainActivity, openCameraIntent)) {
                startAppDetails(openCameraIntent)
                return@setOnClickListener
            }

            val path = filesDir.toString() + File.separator + "images" + File.separator
            val file = File(path, "vcamera-${System.currentTimeMillis()}.jpg")
            if (!file.parentFile.exists())
                file.parentFile.mkdirs()

            mUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                //步骤二：Android 7.0及以上获取文件 Uri
                FileProvider.getUriForFile(this, "top.nightfarmer.vcamera.files", file)
            } else {
                //步骤三：获取文件Uri
                Uri.fromFile(file)
            }

            openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mUri)
            startActivityForResult(openCameraIntent, 110) // 参数常量为自定义的request code, 在取返回结果时有用
        }
        to_take_video.setOnClickListener {
            val openCameraIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE) //系统常量， 启动相机的关键

            if (hasPreferredApplication(this@MainActivity, openCameraIntent)) {
                startAppDetails(openCameraIntent)
                return@setOnClickListener
            }

            val path = filesDir.toString() + File.separator + "images" + File.separator
            val file = File(path, "vcamera-${System.currentTimeMillis()}.mp4")
            if (!file.parentFile.exists())
                file.parentFile.mkdirs()

            mUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                //步骤二：Android 7.0及以上获取文件 Uri
                FileProvider.getUriForFile(this, "top.nightfarmer.vcamera.files", file)
            } else {
                //步骤三：获取文件Uri
                Uri.fromFile(file)
            }

            openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mUri)
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1)
            startActivityForResult(openCameraIntent, 120) // 参数常量为自定义的request code, 在取返回结果时有用
        }
    }

    override fun onResume() {
        super.onResume()
        checkDefaultApp()
    }

    private fun checkDefaultApp() {
        val openCameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE) //系统常量， 启动相机的关键
        if (hasPreferredApplication(this, openCameraIntent)) {
            to_take_pic.text = "重置默认应用"
            tv_notice.text = "相机已设置默认应用，重置默认应用后才会弹出相机选择项"
        } else {
            to_take_pic.text = "测试拍照"
            tv_notice.text = " "
        }
    }

    private fun startAppDetails(intent: Intent) {
        val pm = packageManager
        val info = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        val intent2 = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${info.activityInfo.packageName}"))
        startActivity(intent2)
    }

    private fun hasPreferredApplication(context: Context, intent: Intent): Boolean {
        val pm = context.packageManager
        val info = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        Log.e("xxx", info.activityInfo.packageName)
        return "android" != info.activityInfo.packageName && "top.nightfarmer.vcamera" != info.activityInfo.packageName
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 110) {
                iv_demo.visibility = View.VISIBLE
                vv_demo.visibility = View.GONE
                Glide.with(this).load(mUri).into(iv_demo)
            } else if (requestCode == 120) {
                vv_demo.visibility = View.VISIBLE
                iv_demo.visibility = View.GONE
                vv_demo.setVideoURI(mUri)//为视频播放器设置视频路径
//                vv_demo.setMediaController(MediaController(this@MainActivity))//显示控制栏
                vv_demo.setOnPreparedListener({
                    it.isLooping = true
                    vv_demo.start()//开始播放视频
                })
            }
        }
    }

}

