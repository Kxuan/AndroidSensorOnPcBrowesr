package org.ixuan.vrsensor;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        final Button btn = (Button) findViewById(R.id.btnConnect);

        final EditText etHost = (EditText) findViewById(R.id.etHost),
                etPort = (EditText) findViewById(R.id.etPort);

        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case Connection.MESSAGE_START:
                        Toast.makeText(MainActivity.this, "正在登录", Toast.LENGTH_SHORT).show();
                        break;
                    case Connection.MESSAGE_IOEXCEPTION:
                        Toast.makeText(MainActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
                        btn.setEnabled(true);
                        break;
                    case Connection.MESSAGE_FAIL:
                        Toast.makeText(MainActivity.this, "协商失败", Toast.LENGTH_SHORT).show();
                        btn.setEnabled(true);
                        break;
                    case Connection.MESSAGE_SUCCESS:
                        Toast.makeText(MainActivity.this, "成功", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainActivity.this, PosterActivity.class));
                        btn.setEnabled(true);
                        break;
                    case Connection.MESSAGE_DISCONNECTED:
                        Toast.makeText(MainActivity.this, "连接中断", Toast.LENGTH_SHORT).show();
                        btn.setEnabled(true);
                        break;
                }
                return false;
            }
        });
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    new Connection(handler, etHost.getText().toString(), Integer.parseInt(etPort.getText().toString()));
                } catch (IllegalAccessException e) {
                    Toast.makeText(MainActivity.this, "多实例", Toast.LENGTH_LONG).show();
                    return;
                }
                btn.setEnabled(false);
            }
        });
    }
}
