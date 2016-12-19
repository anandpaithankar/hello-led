package things.android.a.helloled;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;

/**
 * HelloLED.java
 *
 * A sample program to blink an LED connected to a GPIO pin #12 on
 * Intel Edison Development board.
 *
 */
public class HelloLED extends Activity {

    public static final String TAG = HelloLED.class.getSimpleName();

    /*
     * The GPIO Pin number on the board where LED is connected.
     * In this case, LED is connected to PIN # 12 as per the board
     * layout numbering scheme.
     */
    private static final String GPIO_PIN_NAME_LED = "IO12";

    /* LED blinking interval */
    private static final int LED_BLINKING_INTERVAL_MS = 1000; // 1s

    /* Things object representing a LED connection */
    private Gpio mLed;

    private Handler mHandler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // PeripheralService enumerates the connection capabilities
        // of the board.
        PeripheralManagerService peripheralManagerService = new PeripheralManagerService();

        // Get the list of peripherals of GPIO types.
        if (peripheralManagerService.getGpioList().isEmpty()) {
            Log.e(TAG, "Board does not support GPIO connections");
            finish();
        }


        try {

            // Open a GPIO port IO12 and configure the port to be used
            // as a OUTPUT port.
            mLed = peripheralManagerService.openGpio(GPIO_PIN_NAME_LED);
            mLed.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

            // A runnable responsible for blinking the LED
            // at a specified interval.
            mHandler.post(mBlinkerRunnable);

        } catch (IOException e) {
            Log.e(TAG, "Error opening GPIO port " + GPIO_PIN_NAME_LED);
            throw new RuntimeException(e);
        }
    }


    private Runnable mBlinkerRunnable = new Runnable() {
        @Override
        public void run() {
            if (mLed == null) {
                Log.e(TAG, "The LED connection instance is null");
                return;
            }

            try {
                mLed.setValue(!mLed.getValue());  // toggle
                mHandler.postDelayed(mBlinkerRunnable, LED_BLINKING_INTERVAL_MS);
            } catch (IOException e) {
                Log.e(TAG, "LED Blinking error " + e.getMessage());
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Release resources
        mHandler.removeCallbacks(mBlinkerRunnable);
        if (mLed != null) {
            try {
                mLed.close();
            } catch (IOException e) {
                Log.w(TAG, "Error closing GPIO port = " + GPIO_PIN_NAME_LED);
            }
        }
    }
}