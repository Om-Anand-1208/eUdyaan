# eUdyaan redesign

The launcher now opens the Compose redesign in `app/src/main/java/com/example/eudayan/redesign/`. The shared UI includes the header, bottom navigation, cards, buttons, mood faces, doctor portraits, and botanical artwork. The twelve supplied concepts are represented as app routes; splash, onboarding, login, sign-up, the post composer/detail, resource practice, and settings screens extend the same visual language.

The doctor portraits and original brand wreath were extracted from the supplied concepts. The current logo is a refined version of that leafy wreath, used in the header, splash, sign-in, and adaptive launcher icon. The mood reactions and botanical decoration were generated as reusable transparent assets in `app/src/main/res/drawable/`. Three outlined-character illustrations explain check-ins, community, and counsellor support across onboarding, chat, resources, and empty states.

Rubik from `Downloads/Rubik.zip` is the app-wide text family. The supplied `Awesome.zip` font is used for the eUdyaan wordmark **in debug builds only**. Its bundled license allows personal use but requires written permission for commercial mobile apps, so the release variant substitutes Rubik Bold until a commercial license is obtained. The original license texts are retained in `licenses/`.

Sign-in and sign-up call the existing account service at `http://10.0.2.2:5000/` in debug builds. Guest access is available when that service is not running. Community posts, replies, likes, and ManSathi messages are local demo interactions that persist across app restarts. Session requests are saved on the device, without claiming that a counsellor has received them. Mood check-ins are saved by date on the device and shown in the live calendar. Display name, anonymous-posting choice, session date, and dark-mode choice are persisted. Settings has separate Appearance, Privacy & data, Local data, and About screens; Local data controls clear stored check-ins, session requests, Community activity, and ManSathi messages. Release builds disable cleartext traffic and backups, and the direct route preview extra only works in debug builds.

## Before a public release

The design is suitable for further product testing, but the app still needs a deployed HTTPS account service, real appointment availability and confirmation, moderated community storage, a clinically reviewed ManSathi service and crisis support flow, account recovery, and a complete privacy/data retention policy. Sensitive local content currently uses regular app preferences; a public release should use reviewed storage and deletion controls. These features must be connected and tested before presenting the app as a live mental health service. The current guest content is local to this device.

## Build

```bash
ANDROID_HOME=/home/psykr/Android/Sdk \
ANDROID_SDK_ROOT=/home/psykr/Android/Sdk \
JAVA_HOME=/home/psykr/.gradle/jdks/eclipse_adoptium-17-amd64-linux.2 \
bash gradlew --no-daemon assembleDebug
```

## Visual verification

`verification/slim-avd-polish/` and `verification/slim-avd-theme/` contain real 852×1844 screenshots from `Android_API36_Slim`, including dark-mode route captures, persistence checks, and contact sheets. The emulator was booted on the host with a temporary data directory and the host GPU. ADB reported `device` and `sys.boot_completed=1` before installation and capture.

To open a route directly for inspection:

```bash
adb shell am force-stop com.example.eudayan
adb shell am start -n com.example.eudayan/.MainActivity --es screen mood_checkin
```

Route names: `splash`, `onboarding`, `login`, `signup`, `home`, `community`, `post_detail`, `write_post`, `chat`, `support`, `mood_log`, `mood_checkin`, `mood_saved`, `doctors`, `doctor_detail`, `booking`, `notifications`, `profile`, `settings`, `personal`, `security`, `privacy`, `appearance`, `data`, `about`, `breathing`, `meditation`, and `articles`.
