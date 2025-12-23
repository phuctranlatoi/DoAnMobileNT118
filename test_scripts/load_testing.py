#!/usr/bin/env python3
"""
Load Testing Script for Doctor-Patient Messaging System
Tests concurrent users, storage capacity, and system limits
"""

import asyncio
import aiohttp
import json
import time
import random
import string
import threading
from datetime import datetime
from concurrent.futures import ThreadPoolExecutor
import firebase_admin
from firebase_admin import credentials, firestore
import os

class LoadTester:
    def __init__(self):
        self.base_url = "https://your-firebase-project.firebaseapp.com"
        self.results = {
            'concurrent_users': [],
            'storage_tests': [],
            'response_times': [],
            'errors': []
        }
        self.test_start_time = None
        
    def setup_firebase(self):
        """Setup Firebase connection for testing"""
        try:
            # Initialize Firebase Admin SDK
            # You need to download service account key from Firebase Console
            cred_path = "firebase-service-account.json"
            if os.path.exists(cred_path):
                cred = credentials.Certificate(cred_path)
                firebase_admin.initialize_app(cred)
                self.db = firestore.client()
                print("Firebase initialized successfully")
                return True
            else:
                print("Firebase service account key not found")
                print("Download from Firebase Console > Project Settings > Service Accounts")
                return False
        except Exception as e:
            print(f"Firebase setup failed: {e}")
            return False
    
    def generate_test_user(self, user_type="patient", user_id=None):
        """Generate test user data"""
        if user_id is None:
            user_id = ''.join(random.choices(string.ascii_lowercase + string.digits, k=8))
        
        if user_type == "patient":
            return {
                'id': f"BN{user_id}",
                'name': f"Test Patient {user_id}",
                'phone': f"09{random.randint(10000000, 99999999)}",
                'type': 'patient'
            }
        else:
            return {
                'id': f"BS{user_id}",
                'name': f"Dr. Test {user_id}",
                'phone': f"09{random.randint(10000000, 99999999)}",
                'specialty': 'General',
                'type': 'doctor'
            }
    
    def generate_test_message(self, patient_id, doctor_id, message_id=None):
        """Generate test message data"""
        messages = [
            "Xin chào bác sĩ, tôi cần tư vấn",
            "Tôi bị đau đầu từ hôm qua",
            "Có nên uống thuốc gì không ạ?",
            "Cảm ơn bác sĩ đã tư vấn",
            "Tôi sẽ làm theo lời khuyên của bác sĩ"
        ]
        
        return {
            'noiDung': random.choice(messages),
            'maBenhNhan': patient_id,
            'maBacSi': doctor_id,
            'loaiTinNhan': random.choice(['BENH_NHAN', 'BAC_SI']),
            'trangThai': 'DA_GUI',
            'thoiGianGui': firestore.SERVER_TIMESTAMP,
            'tenNguoiGui': f"Test User {random.randint(1, 1000)}"
        }
    
    async def simulate_user_session(self, session, user_data, duration_minutes=5):
        """Simulate a user session with multiple actions"""
        session_start = time.time()
        session_end = session_start + (duration_minutes * 60)
        actions_performed = 0
        
        try:
            while time.time() < session_end:
                # Simulate different user actions
                action = random.choice(['send_message', 'read_messages', 'update_profile'])
                
                if action == 'send_message':
                    await self.simulate_send_message(session, user_data)
                elif action == 'read_messages':
                    await self.simulate_read_messages(session, user_data)
                elif action == 'update_profile':
                    await self.simulate_update_profile(session, user_data)
                
                actions_performed += 1
                
                # Random delay between actions (1-5 seconds)
                await asyncio.sleep(random.uniform(1, 5))
            
            return {
                'user_id': user_data['id'],
                'actions_performed': actions_performed,
                'session_duration': duration_minutes,
                'success': True
            }
            
        except Exception as e:
            return {
                'user_id': user_data['id'],
                'actions_performed': actions_performed,
                'error': str(e),
                'success': False
            }
    
    async def simulate_send_message(self, session, user_data):
        """Simulate sending a message"""
        start_time = time.time()
        
        try:
            # Create test message
            if user_data['type'] == 'patient':
                doctor_id = f"BS{random.randint(1, 100)}"
                message = self.generate_test_message(user_data['id'], doctor_id)
            else:
                patient_id = f"BN{random.randint(1, 1000)}"
                message = self.generate_test_message(patient_id, user_data['id'])
            
            # Add to Firestore (simulated)
            if hasattr(self, 'db'):
                doc_ref = self.db.collection('tin_nhan_bac_si').document()
                doc_ref.set(message)
            
            response_time = time.time() - start_time
            self.results['response_times'].append({
                'action': 'send_message',
                'response_time': response_time,
                'user_id': user_data['id']
            })
            
        except Exception as e:
            self.results['errors'].append({
                'action': 'send_message',
                'error': str(e),
                'user_id': user_data['id']
            })
    
    async def simulate_read_messages(self, session, user_data):
        """Simulate reading messages"""
        start_time = time.time()
        
        try:
            # Query messages (simulated)
            if hasattr(self, 'db'):
                if user_data['type'] == 'patient':
                    query = self.db.collection('tin_nhan_bac_si').where('maBenhNhan', '==', user_data['id']).limit(50)
                else:
                    query = self.db.collection('tin_nhan_bac_si').where('maBacSi', '==', user_data['id']).limit(50)
                
                docs = query.stream()
                message_count = sum(1 for _ in docs)
            
            response_time = time.time() - start_time
            self.results['response_times'].append({
                'action': 'read_messages',
                'response_time': response_time,
                'user_id': user_data['id']
            })
            
        except Exception as e:
            self.results['errors'].append({
                'action': 'read_messages',
                'error': str(e),
                'user_id': user_data['id']
            })
    
    async def simulate_update_profile(self, session, user_data):
        """Simulate updating user profile"""
        start_time = time.time()
        
        try:
            # Update profile (simulated)
            if hasattr(self, 'db'):
                collection = 'benh_nhan' if user_data['type'] == 'patient' else 'bac_si'
                doc_ref = self.db.collection(collection).document(user_data['id'])
                doc_ref.update({
                    'lastActive': firestore.SERVER_TIMESTAMP,
                    'profileUpdated': True
                })
            
            response_time = time.time() - start_time
            self.results['response_times'].append({
                'action': 'update_profile',
                'response_time': response_time,
                'user_id': user_data['id']
            })
            
        except Exception as e:
            self.results['errors'].append({
                'action': 'update_profile',
                'error': str(e),
                'user_id': user_data['id']
            })
    
    async def test_concurrent_users(self, max_users=1000, ramp_up_time=300):
        """Test system with increasing number of concurrent users"""
        print(f"Starting concurrent users test: {max_users} users over {ramp_up_time} seconds")
        
        async with aiohttp.ClientSession() as session:
            tasks = []
            
            for i in range(max_users):
                # Create test user
                user_type = 'patient' if i % 4 != 0 else 'doctor'  # 75% patients, 25% doctors
                user_data = self.generate_test_user(user_type, str(i))
                
                # Create user session task
                task = asyncio.create_task(
                    self.simulate_user_session(session, user_data, duration_minutes=10)
                )
                tasks.append(task)
                
                # Ramp up gradually
                if i % 10 == 0:  # Every 10 users
                    await asyncio.sleep(ramp_up_time / (max_users / 10))
                    print(f"Started {i+1} users...")
            
            # Wait for all sessions to complete
            print("Waiting for all user sessions to complete...")
            results = await asyncio.gather(*tasks, return_exceptions=True)
            
            # Analyze results
            successful_sessions = [r for r in results if isinstance(r, dict) and r.get('success')]
            failed_sessions = [r for r in results if isinstance(r, dict) and not r.get('success')]
            
            self.results['concurrent_users'] = {
                'total_users': max_users,
                'successful_sessions': len(successful_sessions),
                'failed_sessions': len(failed_sessions),
                'success_rate': len(successful_sessions) / max_users * 100,
                'total_actions': sum(s.get('actions_performed', 0) for s in successful_sessions)
            }
            
            print(f"Concurrent users test completed:")
            print(f"- Total users: {max_users}")
            print(f"- Successful sessions: {len(successful_sessions)}")
            print(f"- Failed sessions: {len(failed_sessions)}")
            print(f"- Success rate: {len(successful_sessions) / max_users * 100:.2f}%")
    
    def test_storage_capacity(self, target_messages=100000):
        """Test storage capacity by creating large amounts of data"""
        print(f"Starting storage capacity test: {target_messages} messages")
        
        if not hasattr(self, 'db'):
            print("Firebase not initialized, skipping storage test")
            return
        
        batch_size = 500
        batches = target_messages // batch_size
        
        start_time = time.time()
        total_created = 0
        
        try:
            for batch_num in range(batches):
                batch = self.db.batch()
                
                for i in range(batch_size):
                    patient_id = f"BN{random.randint(1, 1000)}"
                    doctor_id = f"BS{random.randint(1, 100)}"
                    message = self.generate_test_message(patient_id, doctor_id)
                    
                    doc_ref = self.db.collection('tin_nhan_bac_si').document()
                    batch.set(doc_ref, message)
                
                # Commit batch
                batch.commit()
                total_created += batch_size
                
                if batch_num % 10 == 0:
                    elapsed = time.time() - start_time
                    rate = total_created / elapsed
                    print(f"Created {total_created} messages ({rate:.2f} messages/sec)")
        
        except Exception as e:
            print(f"Storage test failed after {total_created} messages: {e}")
        
        total_time = time.time() - start_time
        
        self.results['storage_tests'] = {
            'target_messages': target_messages,
            'created_messages': total_created,
            'total_time': total_time,
            'messages_per_second': total_created / total_time,
            'success': total_created >= target_messages * 0.9  # 90% success threshold
        }
        
        print(f"Storage capacity test completed:")
        print(f"- Target messages: {target_messages}")
        print(f"- Created messages: {total_created}")
        print(f"- Time taken: {total_time:.2f} seconds")
        print(f"- Rate: {total_created / total_time:.2f} messages/second")
    
    def test_database_queries(self, query_count=10000):
        """Test database query performance under load"""
        print(f"Starting database query test: {query_count} queries")
        
        if not hasattr(self, 'db'):
            print("Firebase not initialized, skipping query test")
            return
        
        query_times = []
        successful_queries = 0
        
        for i in range(query_count):
            start_time = time.time()
            
            try:
                # Random query type
                query_type = random.choice(['by_patient', 'by_doctor', 'recent_messages'])
                
                if query_type == 'by_patient':
                    patient_id = f"BN{random.randint(1, 1000)}"
                    query = self.db.collection('tin_nhan_bac_si').where('maBenhNhan', '==', patient_id).limit(50)
                elif query_type == 'by_doctor':
                    doctor_id = f"BS{random.randint(1, 100)}"
                    query = self.db.collection('tin_nhan_bac_si').where('maBacSi', '==', doctor_id).limit(50)
                else:  # recent_messages
                    query = self.db.collection('tin_nhan_bac_si').order_by('thoiGianGui', direction=firestore.Query.DESCENDING).limit(20)
                
                # Execute query
                docs = list(query.stream())
                
                query_time = time.time() - start_time
                query_times.append(query_time)
                successful_queries += 1
                
                if i % 1000 == 0:
                    avg_time = sum(query_times) / len(query_times)
                    print(f"Completed {i} queries, avg time: {avg_time:.3f}s")
            
            except Exception as e:
                print(f"Query {i} failed: {e}")
        
        if query_times:
            avg_query_time = sum(query_times) / len(query_times)
            max_query_time = max(query_times)
            min_query_time = min(query_times)
            
            print(f"Database query test completed:")
            print(f"- Total queries: {query_count}")
            print(f"- Successful queries: {successful_queries}")
            print(f"- Success rate: {successful_queries / query_count * 100:.2f}%")
            print(f"- Average query time: {avg_query_time:.3f}s")
            print(f"- Min query time: {min_query_time:.3f}s")
            print(f"- Max query time: {max_query_time:.3f}s")
    
    def generate_load_test_report(self):
        """Generate comprehensive load test report"""
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        report_file = f"test-reports/load_test_report_{timestamp}.json"
        
        # Create reports directory
        os.makedirs("test-reports", exist_ok=True)
        
        # Compile report data
        report = {
            'test_timestamp': timestamp,
            'test_duration': time.time() - self.test_start_time if self.test_start_time else 0,
            'concurrent_users_test': self.results.get('concurrent_users', {}),
            'storage_test': self.results.get('storage_tests', {}),
            'response_times': {
                'count': len(self.results['response_times']),
                'average': sum(r['response_time'] for r in self.results['response_times']) / len(self.results['response_times']) if self.results['response_times'] else 0,
                'max': max(r['response_time'] for r in self.results['response_times']) if self.results['response_times'] else 0,
                'min': min(r['response_time'] for r in self.results['response_times']) if self.results['response_times'] else 0
            },
            'errors': {
                'count': len(self.results['errors']),
                'error_types': {}
            }
        }
        
        # Analyze error types
        for error in self.results['errors']:
            error_type = error.get('action', 'unknown')
            if error_type not in report['errors']['error_types']:
                report['errors']['error_types'][error_type] = 0
            report['errors']['error_types'][error_type] += 1
        
        # Save report
        with open(report_file, 'w') as f:
            json.dump(report, f, indent=2)
        
        # Generate human-readable report
        readable_report = f"test-reports/load_test_report_{timestamp}.txt"
        with open(readable_report, 'w') as f:
            f.write("LOAD TESTING REPORT\n")
            f.write("=" * 50 + "\n\n")
            f.write(f"Test Date: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n")
            f.write(f"Test Duration: {report['test_duration']:.2f} seconds\n\n")
            
            # Concurrent Users Test
            if 'concurrent_users' in self.results:
                cu = self.results['concurrent_users']
                f.write("CONCURRENT USERS TEST:\n")
                f.write(f"- Total Users: {cu.get('total_users', 0)}\n")
                f.write(f"- Successful Sessions: {cu.get('successful_sessions', 0)}\n")
                f.write(f"- Failed Sessions: {cu.get('failed_sessions', 0)}\n")
                f.write(f"- Success Rate: {cu.get('success_rate', 0):.2f}%\n")
                f.write(f"- Total Actions: {cu.get('total_actions', 0)}\n\n")
            
            # Storage Test
            if 'storage_tests' in self.results:
                st = self.results['storage_tests']
                f.write("STORAGE CAPACITY TEST:\n")
                f.write(f"- Target Messages: {st.get('target_messages', 0)}\n")
                f.write(f"- Created Messages: {st.get('created_messages', 0)}\n")
                f.write(f"- Time Taken: {st.get('total_time', 0):.2f} seconds\n")
                f.write(f"- Messages/Second: {st.get('messages_per_second', 0):.2f}\n")
                f.write(f"- Success: {'YES' if st.get('success') else 'NO'}\n\n")
            
            # Response Times
            f.write("RESPONSE TIMES:\n")
            f.write(f"- Total Requests: {report['response_times']['count']}\n")
            f.write(f"- Average Response Time: {report['response_times']['average']:.3f}s\n")
            f.write(f"- Min Response Time: {report['response_times']['min']:.3f}s\n")
            f.write(f"- Max Response Time: {report['response_times']['max']:.3f}s\n\n")
            
            # Errors
            f.write("ERRORS:\n")
            f.write(f"- Total Errors: {report['errors']['count']}\n")
            for error_type, count in report['errors']['error_types'].items():
                f.write(f"- {error_type}: {count}\n")
        
        print(f"Load test report saved to: {report_file}")
        print(f"Human-readable report: {readable_report}")
        
        return report

async def main():
    """Main function to run load tests"""
    print("Doctor-Patient Messaging System Load Testing")
    print("=" * 50)
    
    tester = LoadTester()
    tester.test_start_time = time.time()
    
    # Setup Firebase (optional)
    firebase_available = tester.setup_firebase()
    
    # Test configurations
    test_configs = {
        'small_load': {'users': 50, 'messages': 5000},
        'medium_load': {'users': 200, 'messages': 20000},
        'large_load': {'users': 500, 'messages': 50000},
        'stress_test': {'users': 1000, 'messages': 100000}
    }
    
    # Choose test level
    test_level = input("Choose test level (small/medium/large/stress): ").lower()
    if test_level not in test_configs:
        test_level = 'small_load'
        print("Invalid choice, using small_load")
    
    config = test_configs[test_level]
    
    print(f"Running {test_level} test:")
    print(f"- Concurrent users: {config['users']}")
    print(f"- Storage messages: {config['messages']}")
    print()
    
    # Run tests
    try:
        # Test 1: Concurrent Users
        await tester.test_concurrent_users(
            max_users=config['users'],
            ramp_up_time=60  # 1 minute ramp-up
        )
        
        # Test 2: Storage Capacity (if Firebase available)
        if firebase_available:
            tester.test_storage_capacity(target_messages=config['messages'])
            tester.test_database_queries(query_count=config['messages'] // 10)
        
        # Generate report
        report = tester.generate_load_test_report()
        
        print("\nLOAD TESTING COMPLETED!")
        print("Check the generated reports for detailed results.")
        
    except KeyboardInterrupt:
        print("\nLoad testing interrupted by user")
    except Exception as e:
        print(f"Load testing failed: {e}")

if __name__ == "__main__":
    asyncio.run(main())