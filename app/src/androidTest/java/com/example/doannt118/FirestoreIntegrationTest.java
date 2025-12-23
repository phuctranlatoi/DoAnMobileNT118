package com.example.doannt118;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.doannt118.model.TinNhanBacSi;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

/**
 * Integration tests for Firebase Firestore
 * Tests database operations and real-time functionality
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class FirestoreIntegrationTest {
    
    private FirebaseFirestore db;
    private CountDownLatch latch;
    
    @Before
    public void setUp() {
        db = FirebaseFirestore.getInstance();
        latch = new CountDownLatch(1);
    }
    
    @Test
    public void testFirestore_Connection_IsWorking() throws InterruptedException {
        // Test basic Firestore connection
        db.collection("test")
          .limit(1)
          .get()
          .addOnCompleteListener(task -> {
              // Connection test - should complete without error
              assertThat(task.isComplete()).isTrue();
              latch.countDown();
          });
        
        boolean completed = latch.await(10, TimeUnit.SECONDS);
        assertThat(completed).isTrue();
    }
    
    @Test
    public void testSaveMessage_ValidMessage_SavedSuccessfully() throws InterruptedException {
        // Arrange
        TinNhanBacSi tinNhan = new TinNhanBacSi(
            "Test message from integration test",
            "patient_test_" + System.currentTimeMillis(),
            "doctor_test_" + System.currentTimeMillis(),
            new Date()
        );
        
        // Act
        db.collection("tin_nhan_bac_si")
          .add(tinNhan)
          .addOnSuccessListener(documentReference -> {
              assertThat(documentReference.getId()).isNotNull();
              assertThat(documentReference.getId()).isNotEmpty();
              latch.countDown();
          })
          .addOnFailureListener(e -> {
              fail("Should not fail: " + e.getMessage());
              latch.countDown();
          });
        
        // Assert
        boolean completed = latch.await(15, TimeUnit.SECONDS);
        assertThat(completed).isTrue();
    }
    
    @Test
    public void testReadMessages_ValidQuery_ReturnsResults() throws InterruptedException {
        // First, save a test message
        String testConversationId = "test_conversation_" + System.currentTimeMillis();
        TinNhanBacSi testMessage = new TinNhanBacSi(
            "Test query message",
            "patient_query_test",
            "doctor_query_test", 
            new Date()
        );
        
        // Save the message first
        CountDownLatch saveLatch = new CountDownLatch(1);
        db.collection("tin_nhan_bac_si")
          .add(testMessage)
          .addOnSuccessListener(documentReference -> saveLatch.countDown())
          .addOnFailureListener(e -> saveLatch.countDown());
        
        saveLatch.await(10, TimeUnit.SECONDS);
        
        // Now query for messages
        db.collection("tin_nhan_bac_si")
          .whereEqualTo("nguoiGui", "patient_query_test")
          .get()
          .addOnSuccessListener(querySnapshot -> {
              assertThat(querySnapshot).isNotNull();
              // Should have at least our test message
              assertThat(querySnapshot.size()).isAtLeast(1);
              latch.countDown();
          })
          .addOnFailureListener(e -> {
              fail("Query should not fail: " + e.getMessage());
              latch.countDown();
          });
        
        boolean completed = latch.await(15, TimeUnit.SECONDS);
        assertThat(completed).isTrue();
    }
    
    @Test
    public void testRealtimeListener_MessageUpdates_ReceivesUpdates() throws InterruptedException {
        String testUserId = "realtime_test_" + System.currentTimeMillis();
        
        // Set up real-time listener
        db.collection("tin_nhan_bac_si")
          .whereEqualTo("nguoiGui", testUserId)
          .addSnapshotListener((querySnapshot, e) -> {
              if (e != null) {
                  fail("Listener should not fail: " + e.getMessage());
                  latch.countDown();
                  return;
              }
              
              if (querySnapshot != null && !querySnapshot.isEmpty()) {
                  // Received update
                  assertThat(querySnapshot.size()).isAtLeast(1);
                  latch.countDown();
              }
          });
        
        // Wait a moment for listener to be established
        Thread.sleep(1000);
        
        // Add a message to trigger the listener
        TinNhanBacSi realtimeMessage = new TinNhanBacSi(
            "Realtime test message",
            testUserId,
            "doctor_realtime_test",
            new Date()
        );
        
        db.collection("tin_nhan_bac_si").add(realtimeMessage);
        
        // Wait for real-time update
        boolean completed = latch.await(20, TimeUnit.SECONDS);
        assertThat(completed).isTrue();
    }
    
    @Test
    public void testBatchWrite_MultipleMessages_AllSaved() throws InterruptedException {
        // Test batch writing multiple messages
        String batchTestId = "batch_test_" + System.currentTimeMillis();
        
        // Create multiple test messages
        TinNhanBacSi[] messages = {
            new TinNhanBacSi("Batch message 1", batchTestId, "doctor_batch", new Date()),
            new TinNhanBacSi("Batch message 2", batchTestId, "doctor_batch", new Date()),
            new TinNhanBacSi("Batch message 3", batchTestId, "doctor_batch", new Date())
        };
        
        // Save all messages
        CountDownLatch batchLatch = new CountDownLatch(messages.length);
        
        for (TinNhanBacSi message : messages) {
            db.collection("tin_nhan_bac_si")
              .add(message)
              .addOnCompleteListener(task -> batchLatch.countDown());
        }
        
        // Wait for all saves to complete
        boolean allSaved = batchLatch.await(20, TimeUnit.SECONDS);
        assertThat(allSaved).isTrue();
        
        // Query to verify all messages were saved
        db.collection("tin_nhan_bac_si")
          .whereEqualTo("nguoiGui", batchTestId)
          .get()
          .addOnSuccessListener(querySnapshot -> {
              assertThat(querySnapshot.size()).isEqualTo(messages.length);
              latch.countDown();
          })
          .addOnFailureListener(e -> {
              fail("Batch query failed: " + e.getMessage());
              latch.countDown();
          });
        
        boolean completed = latch.await(15, TimeUnit.SECONDS);
        assertThat(completed).isTrue();
    }
    
    @Test
    public void testMessageOrdering_ByTimestamp_OrderedCorrectly() throws InterruptedException {
        String orderTestId = "order_test_" + System.currentTimeMillis();
        
        // Create messages with different timestamps
        Date now = new Date();
        Date earlier = new Date(now.getTime() - 60000); // 1 minute earlier
        Date later = new Date(now.getTime() + 60000);   // 1 minute later
        
        TinNhanBacSi[] messages = {
            new TinNhanBacSi("Later message", orderTestId, "doctor_order", later),
            new TinNhanBacSi("Earlier message", orderTestId, "doctor_order", earlier),
            new TinNhanBacSi("Now message", orderTestId, "doctor_order", now)
        };
        
        // Save all messages
        CountDownLatch saveLatch = new CountDownLatch(messages.length);
        for (TinNhanBacSi message : messages) {
            db.collection("tin_nhan_bac_si")
              .add(message)
              .addOnCompleteListener(task -> saveLatch.countDown());
        }
        
        saveLatch.await(15, TimeUnit.SECONDS);
        
        // Query messages ordered by timestamp
        db.collection("tin_nhan_bac_si")
          .whereEqualTo("nguoiGui", orderTestId)
          .orderBy("thoiGian")
          .get()
          .addOnSuccessListener(querySnapshot -> {
              assertThat(querySnapshot.size()).isEqualTo(3);
              
              // Verify ordering
              Date previousTime = null;
              for (int i = 0; i < querySnapshot.size(); i++) {
                  Date currentTime = querySnapshot.getDocuments().get(i).getDate("thoiGian");
                  if (previousTime != null) {
                      assertThat(currentTime.getTime()).isAtLeast(previousTime.getTime());
                  }
                  previousTime = currentTime;
              }
              
              latch.countDown();
          })
          .addOnFailureListener(e -> {
              fail("Ordering query failed: " + e.getMessage());
              latch.countDown();
          });
        
        boolean completed = latch.await(15, TimeUnit.SECONDS);
        assertThat(completed).isTrue();
    }
    
    @Test
    public void testLargeMessage_CanBeSaved() throws InterruptedException {
        // Test saving a large message
        StringBuilder largeContent = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            largeContent.append("This is a large message content for testing purposes. ");
        }
        
        TinNhanBacSi largeMessage = new TinNhanBacSi(
            largeContent.toString(),
            "patient_large_test",
            "doctor_large_test",
            new Date()
        );
        
        db.collection("tin_nhan_bac_si")
          .add(largeMessage)
          .addOnSuccessListener(documentReference -> {
              assertThat(documentReference.getId()).isNotNull();
              latch.countDown();
          })
          .addOnFailureListener(e -> {
              fail("Large message save failed: " + e.getMessage());
              latch.countDown();
          });
        
        boolean completed = latch.await(20, TimeUnit.SECONDS);
        assertThat(completed).isTrue();
    }
    
    @Test
    public void testSpecialCharacters_CanBeSaved() throws InterruptedException {
        // Test saving message with special characters and Vietnamese
        String specialMessage = "Tin nhắn với ký tự đặc biệt: @#$%^&*()_+{}|:<>?[]\\;'\",./ " +
                               "và tiếng Việt có dấu: àáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđ";
        
        TinNhanBacSi specialCharMessage = new TinNhanBacSi(
            specialMessage,
            "patient_special_test",
            "doctor_special_test",
            new Date()
        );
        
        db.collection("tin_nhan_bac_si")
          .add(specialCharMessage)
          .addOnSuccessListener(documentReference -> {
              // Verify the message was saved correctly by reading it back
              documentReference.get().addOnSuccessListener(documentSnapshot -> {
                  String savedContent = documentSnapshot.getString("noiDung");
                  assertThat(savedContent).isEqualTo(specialMessage);
                  latch.countDown();
              });
          })
          .addOnFailureListener(e -> {
              fail("Special characters message save failed: " + e.getMessage());
              latch.countDown();
          });
        
        boolean completed = latch.await(15, TimeUnit.SECONDS);
        assertThat(completed).isTrue();
    }
}