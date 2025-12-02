# 🤖 Hướng dẫn Build AI Chatbot với Gemini

## 📦 Bước 1: Setup Dependencies

### 1.1 Thêm vào `app/build.gradle.kts`:

```kotlin
dependencies {
    // Gemini AI SDK
    implementation("com.google.ai.client.generativeai:generativeai:0.1.2")
    
    // Retrofit cho API calls (nếu cần)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    
    // Coroutines (Gemini SDK cần)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // Existing dependencies...
}
```

### 1.2 Bảo mật API Key

**QUAN TRỌNG:** Không hardcode API key trong code!

#### Option A: Dùng `local.properties` (Khuyên dùng)

1. Thêm vào `local.properties`:
```properties
GEMINI_API_KEY=AIzaSyDV_NQJ6TdqhPVnSKWsDCzEcjl6MQd8Uk4
```

2. Thêm vào `app/build.gradle.kts`:
```kotlin
android {
    defaultConfig {
        // Load API key từ local.properties
        val properties = Properties()
        properties.load(project.rootProject.file("local.properties").inputStream())
        buildConfigField("String", "GEMINI_API_KEY", "\"${properties.getProperty("GEMINI_API_KEY")}\"")
    }
    
    buildFeatures {
        buildConfig = true
    }
}
```

3. Sử dụng trong code:
```java
String apiKey = BuildConfig.GEMINI_API_KEY;
```

#### Option B: Dùng `strings.xml` (Đơn giản hơn cho Java)

1. Tạo file `app/src/main/res/values/secrets.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="gemini_api_key">AIzaSyDV_NQJ6TdqhPVnSKWsDCzEcjl6MQd8Uk4</string>
</resources>
```

2. Thêm vào `.gitignore`:
```
app/src/main/res/values/secrets.xml
```

3. Sử dụng:
```java
String apiKey = getString(R.string.gemini_api_key);
```

---

## 🏗️ Bước 2: Kiến trúc Hệ thống

```
┌─────────────────────────────────────────┐
│         ChatActivity (UI)               │
│  - RecyclerView messages                │
│  - Input field + Send button            │
│  - Quick reply chips                    │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│      GeminiChatbot (AI Engine)          │
│  - Call Gemini API                      │
│  - Manage conversation history          │
│  - Parse responses                      │
└──────────────┬──────────────────────────┘
               │
        ┌──────┴──────┐
        ▼             ▼
┌──────────────┐  ┌──────────────┐
│ Gemini API   │  │ Firestore    │
│ (Cloud)      │  │ (Your Data)  │
└──────────────┘  └──────────────┘
```

---

## 💻 Bước 3: Implementation

### 3.1 Tạo GeminiService.java

```java
package com.example.doannt118.service;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GeminiService {
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent";
    private String apiKey;
    private ExecutorService executor;
    
    public interface GeminiCallback {
        void onSuccess(String response);
        void onError(String error);
    }
    
    public GeminiService(String apiKey) {
        this.apiKey = apiKey;
        this.executor = Executors.newSingleThreadExecutor();
    }
    
    public void sendMessage(String message, String systemPrompt, GeminiCallback callback) {
        executor.execute(() -> {
            try {
                // Build request
                JSONObject requestBody = new JSONObject();
                JSONArray contents = new JSONArray();
                
                // Add system prompt
                if (systemPrompt != null && !systemPrompt.isEmpty()) {
                    JSONObject systemMessage = new JSONObject();
                    JSONArray systemParts = new JSONArray();
                    systemParts.put(new JSONObject().put("text", systemPrompt));
                    systemMessage.put("parts", systemParts);
                    contents.put(systemMessage);
                }
                
                // Add user message
                JSONObject userMessage = new JSONObject();
                JSONArray userParts = new JSONArray();
                userParts.put(new JSONObject().put("text", message));
                userMessage.put("parts", userParts);
                contents.put(userMessage);
                
                requestBody.put("contents", contents);
                
                // Make API call
                URL url = new URL(API_URL + "?key=" + apiKey);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                
                // Send request
                OutputStream os = conn.getOutputStream();
                os.write(requestBody.toString().getBytes());
                os.flush();
                os.close();
                
                // Read response
                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    br.close();
                    
                    // Parse response
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    String text = jsonResponse
                        .getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text");
                    
                    callback.onSuccess(text);
                } else {
                    callback.onError("API Error: " + responseCode);
                }
                
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }
}
```

### 3.2 Tạo ChatMessage.java

```java
package com.example.doannt118.model;

import java.util.Date;

public class ChatMessage {
    public enum MessageType {
        USER, BOT, SYSTEM
    }
    
    private String id;
    private String text;
    private MessageType type;
    private Date timestamp;
    
    public ChatMessage(String text, MessageType type) {
        this.id = String.valueOf(System.currentTimeMillis());
        this.text = text;
        this.type = type;
        this.timestamp = new Date();
    }
    
    // Getters
    public String getId() { return id; }
    public String getText() { return text; }
    public MessageType getType() { return type; }
    public Date getTimestamp() { return timestamp; }
}
```

### 3.3 Tạo System Prompt (Quan trọng!)

```java
package com.example.doannt118.chatbot;

public class SystemPrompts {
    
    public static String getHealthcareAssistantPrompt(String userName, String userContext) {
        return """
            Bạn là trợ lý y tế AI thông minh của phòng khám.
            
            THÔNG TIN NGƯỜI DÙNG:
            - Tên: %s
            - Context: %s
            
            NHIỆM VỤ:
            1. Hỗ trợ đặt lịch khám bệnh
            2. Hướng dẫn uống thuốc theo đơn
            3. Tra cứu thông tin bác sĩ
            4. Xem lịch hẹn và bệnh án
            5. Tư vấn sơ bộ về triệu chứng
            
            QUY TẮC:
            - Luôn lịch sự, thân thiện, chuyên nghiệp
            - Trả lời ngắn gọn, dễ hiểu (2-3 câu)
            - Sử dụng emoji phù hợp: 👨‍⚕️ 💊 📅 ⏰
            - KHÔNG chẩn đoán bệnh, chỉ tư vấn sơ bộ
            - Luôn khuyên gặp bác sĩ nếu nghiêm trọng
            - Nếu cần thông tin từ hệ thống, nói rõ "Tôi sẽ kiểm tra..."
            
            ĐỊNH DẠNG TRẢ LỜI:
            - Ngắn gọn, có cấu trúc
            - Dùng bullet points khi cần
            - Kết thúc bằng câu hỏi hoặc gợi ý hành động
            
            VÍ DỤ:
            User: "Tôi muốn đặt lịch khám"
            Bot: "Tôi sẽ giúp bạn đặt lịch! 📅
                 Bạn muốn khám chuyên khoa nào?
                 - Tim mạch 💓
                 - Nội khoa 🏥
                 - Nhi khoa 👶"
            """.formatted(userName, userContext);
    }
}
```

---

## 🎨 Bước 4: Build UI

### 4.1 Layout: activity_chat.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:background="#F5F5F5">

    <!-- Toolbar -->
    <com.google.android.material.appbar.AppBarLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content">
        
        <androidx.appcompat.widget.Toolbar
            android:id="@+id/toolbar"
            android:layout_width="match_parent"
            android:layout_height="?attr/actionBarSize"
            android:background="?attr/colorPrimary" />
    </com.google.android.material.appbar.AppBarLayout>

    <!-- Chat Messages -->
    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/rvChat"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"
        android:padding="8dp"
        android:clipToPadding="false" />

    <!-- Quick Replies (Optional) -->
    <HorizontalScrollView
        android:id="@+id/quickRepliesContainer"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:padding="8dp"
        android:visibility="gone">
        
        <LinearLayout
            android:id="@+id/quickRepliesLayout"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:orientation="horizontal" />
    </HorizontalScrollView>

    <!-- Input Area -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:padding="8dp"
        android:background="@android:color/white"
        android:elevation="4dp">

        <EditText
            android:id="@+id/edtMessage"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:hint="Nhập tin nhắn..."
            android:padding="12dp"
            android:background="@drawable/bg_edit_text"
            android:maxLines="4" />

        <Button
            android:id="@+id/btnSend"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Gửi"
            android:layout_marginStart="8dp" />
    </LinearLayout>
</LinearLayout>
```

### 4.2 Message Items

**item_chat_user.xml:**
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:padding="8dp"
    android:gravity="end">

    <androidx.cardview.widget.CardView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginStart="64dp"
        app:cardCornerRadius="16dp"
        app:cardBackgroundColor="#2196F3"
        app:cardElevation="2dp">

        <TextView
            android:id="@+id/tvMessage"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:padding="12dp"
            android:textColor="@android:color/white"
            android:textSize="15sp" />
    </androidx.cardview.widget.CardView>
</LinearLayout>
```

**item_chat_bot.xml:**
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:padding="8dp"
    android:orientation="horizontal">

    <ImageView
        android:layout_width="32dp"
        android:layout_height="32dp"
        android:src="@drawable/ic_bot"
        android:layout_marginEnd="8dp" />

    <androidx.cardview.widget.CardView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginEnd="64dp"
        app:cardCornerRadius="16dp"
        app:cardBackgroundColor="@android:color/white"
        app:cardElevation="2dp">

        <TextView
            android:id="@+id/tvMessage"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:padding="12dp"
            android:textColor="@android:color/black"
            android:textSize="15sp" />
    </androidx.cardview.widget.CardView>
</LinearLayout>
```

---

## 🔥 Bước 5: ChatActivity Implementation

Xem file tiếp theo...

---

## 📊 Bước 6: Function Calling (Advanced)

Để chatbot có thể thực hiện actions (đặt lịch, xem thuốc...), dùng Function Calling:

```java
// Gemini sẽ trả về JSON với function name và parameters
// Bạn parse và thực thi function tương ứng

{
  "function_call": {
    "name": "book_appointment",
    "arguments": {
      "doctor_id": "BS001",
      "date": "2024-12-15",
      "time": "08:00"
    }
  }
}
```

---

## 🎯 Bước 7: Testing

1. Test với câu đơn giản: "Xin chào"
2. Test đặt lịch: "Tôi muốn đặt lịch khám"
3. Test xem thuốc: "Thuốc của tôi"
4. Test edge cases: Câu dài, emoji, tiếng Việt có dấu

---

## 📝 Notes

- Gemini API có rate limit: 60 requests/phút (free tier)
- Response time: ~1-3 giây
- Cost: Free tier đủ cho đồ án
- Nên cache responses để giảm API calls

---

Tiếp theo tôi sẽ code đầy đủ ChatActivity!
