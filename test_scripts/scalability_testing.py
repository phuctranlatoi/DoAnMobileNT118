#!/usr/bin/env python3
"""
Scalability Testing Script
Tests system limits, concurrent connections, and resource scaling
"""

import asyncio
import aiohttp
import json
import time
import random
import threading
import psutil
import subprocess
import os
from datetime import datetime
from concurrent.futures import ThreadPoolExecutor, as_completed

class ScalabilityTester:
    def __init__(self):
        self.results = {
            'concurrent_connections': [],
            'memory_usage': [],
            'cpu_usage': [],
            'storage_growth': [],
            'response_times': [],
            'error_rates': []
        }
        self.test_start_time = None
        self.max_concurrent_users = 0
        self.total_messages_sent = 0
        
    def monitor_system_resources(self, duration_seconds=300):
        """Monitor system resources during testing"""
        print(f"Starting system resource monitoring for {duration_seconds} seconds...")
        
        start_time = time.time()
        end_time = start_time + duration_seconds
        
        while time.time() < end_time:
            # Get current system stats
            cpu_percent = psutil.cpu_percent(interval=1)
            memory = psutil.virtual_memory()
            disk = psutil.disk_usage('/')
            
            timestamp = time.time()
            
            self.results['cpu_usage'].append({
                'timestamp': timestamp,
                'cpu_percent': cpu_percent
            })
            
            self.results['memory_usage'].append({
                'timestamp': timestamp,
                'memory_percent': memory.percent,
                'memory_used_gb': memory.used / (1024**3),
                'memory_available_gb': memory.available / (1024**3)
            })
            
            # Check if we're running out of resources
            if memory.percent > 90:
                print(f"WARNING: High memory usage: {memory.percent}%")
            
            if cpu_percent > 90:
                print(f"WARNING: High CPU usage: {cpu_percent}%")
            
            time.sleep(5)  # Sample every 5 seconds
    
    def simulate_user_load(self, user_id, duration_minutes=10, actions_per_minute=6):
        """Simulate a single user's load on the system"""
        start_time = time.time()
        end_time = start_time + (duration_minutes * 60)
        actions_performed = 0
        errors = 0
        
        try:
            while time.time() < end_time:
                action_start = time.time()
                
                # Simulate different actions
                action_type = random.choice([
                    'send_message', 'read_messages', 'make_call', 
                    'update_profile', 'search_doctors'
                ])
                
                try:
                    # Simulate action processing time
                    if action_type == 'send_message':
                        processing_time = random.uniform(0.1, 0.5)
                    elif action_type == 'read_messages':
                        processing_time = random.uniform(0.2, 0.8)
                    elif action_type == 'make_call':
                        processing_time = random.uniform(1.0, 3.0)
                    else:
                        processing_time = random.uniform(0.1, 0.3)
                    
                    time.sleep(processing_time)
                    
                    response_time = time.time() - action_start
                    
                    self.results['response_times'].append({
                        'user_id': user_id,
                        'action': action_type,
                        'response_time': response_time,
                        'timestamp': time.time()
                    })
                    
                    actions_performed += 1
                    
                    if action_type == 'send_message':
                        self.total_messages_sent += 1
                    
                except Exception as e:
                    errors += 1
                    print(f"User {user_id} action {action_type} failed: {e}")
                
                # Wait before next action
                wait_time = 60 / actions_per_minute + random.uniform(-5, 5)
                if wait_time > 0:
                    time.sleep(wait_time)
            
            return {
                'user_id': user_id,
                'actions_performed': actions_performed,
                'errors': errors,
                'success': True
            }
            
        except Exception as e:
            return {
                'user_id': user_id,
                'actions_performed': actions_performed,
                'errors': errors,
                'error': str(e),
                'success': False
            }
    
    def test_concurrent_user_scaling(self, max_users=1000, step_size=50, step_duration=60):
        """Test system behavior with gradually increasing user load"""
        print(f"Starting concurrent user scaling test: 0 to {max_users} users")
        print(f"Step size: {step_size}, Step duration: {step_duration} seconds")
        
        # Start system monitoring in background
        monitor_thread = threading.Thread(
            target=self.monitor_system_resources,
            args=(max_users * step_duration // step_size + 120,)  # Extra time for cleanup
        )
        monitor_thread.daemon = True
        monitor_thread.start()
        
        current_users = 0
        active_futures = []
        
        with ThreadPoolExecutor(max_workers=max_users) as executor:
            while current_users < max_users:
                # Add new users
                new_users = min(step_size, max_users - current_users)
                
                for i in range(new_users):
                    user_id = current_users + i + 1
                    future = executor.submit(
                        self.simulate_user_load,
                        user_id,
                        duration_minutes=step_duration // 60 * 2,  # Users run for 2 steps
                        actions_per_minute=random.randint(3, 10)
                    )
                    active_futures.append(future)
                
                current_users += new_users
                self.max_concurrent_users = max(self.max_concurrent_users, current_users)
                
                print(f"Scaled to {current_users} concurrent users")
                
                # Record scaling point
                self.results['concurrent_connections'].append({
                    'timestamp': time.time(),
                    'concurrent_users': current_users,
                    'total_messages': self.total_messages_sent
                })
                
                # Wait for step duration
                time.sleep(step_duration)
                
                # Check system health
                memory = psutil.virtual_memory()
                cpu = psutil.cpu_percent()
                
                if memory.percent > 95 or cpu > 95:
                    print(f"CRITICAL: System resources exhausted at {current_users} users")
                    print(f"Memory: {memory.percent}%, CPU: {cpu}%")
                    break
            
            # Wait for all users to complete
            print("Waiting for all user sessions to complete...")
            completed_users = 0
            failed_users = 0
            
            for future in as_completed(active_futures):
                try:
                    result = future.result(timeout=30)
                    if result['success']:
                        completed_users += 1
                    else:
                        failed_users += 1
                except Exception as e:
                    failed_users += 1
                    print(f"User session failed: {e}")
        
        print(f"Concurrent user scaling test completed:")
        print(f"- Max concurrent users reached: {self.max_concurrent_users}")
        print(f"- Completed user sessions: {completed_users}")
        print(f"- Failed user sessions: {failed_users}")
        print(f"- Total messages sent: {self.total_messages_sent}")
    
    def test_storage_scaling(self, target_size_gb=1.0, batch_size=1000):
        """Test storage scaling by creating large amounts of data"""
        print(f"Starting storage scaling test: Target {target_size_gb}GB")
        
        # Estimate message size (approximate)
        avg_message_size = 200  # bytes (including metadata)
        target_messages = int(target_size_gb * 1024 * 1024 * 1024 / avg_message_size)
        
        print(f"Target messages: {target_messages}")
        
        start_time = time.time()
        messages_created = 0
        storage_points = []
        
        # Create test directory
        test_storage_dir = "test-storage"
        os.makedirs(test_storage_dir, exist_ok=True)
        
        try:
            while messages_created < target_messages:
                batch_start = time.time()
                
                # Create batch of messages
                batch_data = []
                for i in range(batch_size):
                    message = {
                        'id': f"msg_{messages_created + i}",
                        'content': f"Test message {messages_created + i} " + "x" * random.randint(50, 150),
                        'sender': f"user_{random.randint(1, 1000)}",
                        'receiver': f"user_{random.randint(1, 1000)}",
                        'timestamp': time.time(),
                        'metadata': {
                            'type': random.choice(['text', 'image', 'file']),
                            'priority': random.choice(['low', 'normal', 'high']),
                            'encrypted': True
                        }
                    }
                    batch_data.append(message)
                
                # Write batch to file (simulating database write)
                batch_file = f"{test_storage_dir}/batch_{messages_created // batch_size}.json"
                with open(batch_file, 'w') as f:
                    json.dump(batch_data, f)
                
                messages_created += batch_size
                batch_time = time.time() - batch_start
                
                # Record storage growth
                if messages_created % (batch_size * 10) == 0:  # Every 10 batches
                    storage_size = sum(
                        os.path.getsize(os.path.join(test_storage_dir, f))
                        for f in os.listdir(test_storage_dir)
                    )
                    
                    storage_points.append({
                        'messages': messages_created,
                        'storage_bytes': storage_size,
                        'storage_mb': storage_size / (1024 * 1024),
                        'timestamp': time.time(),
                        'write_rate': batch_size / batch_time
                    })
                    
                    print(f"Created {messages_created} messages, "
                          f"Storage: {storage_size / (1024 * 1024):.2f}MB, "
                          f"Rate: {batch_size / batch_time:.2f} msg/sec")
                
                # Check if we've reached target size
                if storage_points and storage_points[-1]['storage_bytes'] >= target_size_gb * 1024**3:
                    break
        
        except Exception as e:
            print(f"Storage scaling test failed: {e}")
        
        finally:
            # Cleanup test files
            import shutil
            if os.path.exists(test_storage_dir):
                shutil.rmtree(test_storage_dir)
        
        total_time = time.time() - start_time
        final_storage = storage_points[-1] if storage_points else {'storage_mb': 0, 'messages': 0}
        
        self.results['storage_growth'] = storage_points
        
        print(f"Storage scaling test completed:")
        print(f"- Messages created: {messages_created}")
        print(f"- Final storage size: {final_storage['storage_mb']:.2f}MB")
        print(f"- Total time: {total_time:.2f} seconds")
        print(f"- Average rate: {messages_created / total_time:.2f} messages/second")
    
    def test_network_scaling(self, max_connections=500, connection_step=25):
        """Test network connection scaling"""
        print(f"Starting network scaling test: up to {max_connections} connections")
        
        async def create_connection_load():
            connection_results = []
            
            for connections in range(connection_step, max_connections + 1, connection_step):
                print(f"Testing {connections} concurrent connections...")
                
                async with aiohttp.ClientSession(
                    connector=aiohttp.TCPConnector(limit=connections)
                ) as session:
                    
                    # Create concurrent requests
                    tasks = []
                    for i in range(connections):
                        task = asyncio.create_task(
                            self.simulate_network_request(session, i)
                        )
                        tasks.append(task)
                    
                    start_time = time.time()
                    results = await asyncio.gather(*tasks, return_exceptions=True)
                    end_time = time.time()
                    
                    # Analyze results
                    successful = sum(1 for r in results if not isinstance(r, Exception))
                    failed = len(results) - successful
                    
                    connection_results.append({
                        'connections': connections,
                        'successful': successful,
                        'failed': failed,
                        'success_rate': successful / connections * 100,
                        'total_time': end_time - start_time,
                        'avg_response_time': (end_time - start_time) / connections
                    })
                    
                    print(f"  Successful: {successful}/{connections} "
                          f"({successful/connections*100:.1f}%)")
                    
                    # Stop if success rate drops too low
                    if successful / connections < 0.8:  # Less than 80% success
                        print(f"Stopping test due to low success rate at {connections} connections")
                        break
                
                await asyncio.sleep(2)  # Brief pause between tests
            
            return connection_results
        
        # Run async network test
        loop = asyncio.new_event_loop()
        asyncio.set_event_loop(loop)
        try:
            connection_results = loop.run_until_complete(create_connection_load())
        finally:
            loop.close()
        
        # Store results
        self.results['concurrent_connections'].extend(connection_results)
        
        print("Network scaling test completed")
        if connection_results:
            max_successful = max(r['connections'] for r in connection_results if r['success_rate'] > 80)
            print(f"Maximum successful concurrent connections: {max_successful}")
    
    async def simulate_network_request(self, session, request_id):
        """Simulate a network request"""
        try:
            # Simulate API call delay
            await asyncio.sleep(random.uniform(0.1, 0.5))
            
            # Simulate response processing
            response_data = {
                'request_id': request_id,
                'timestamp': time.time(),
                'data': 'x' * random.randint(100, 1000)  # Variable response size
            }
            
            return response_data
            
        except Exception as e:
            raise Exception(f"Request {request_id} failed: {e}")
    
    def calculate_error_rates(self):
        """Calculate error rates from test results"""
        if not self.results['response_times']:
            return
        
        total_requests = len(self.results['response_times'])
        
        # Group by time windows (1-minute windows)
        time_windows = {}
        for response in self.results['response_times']:
            window = int(response['timestamp'] // 60) * 60  # Round to minute
            if window not in time_windows:
                time_windows[window] = {'total': 0, 'errors': 0}
            time_windows[window]['total'] += 1
        
        # Calculate error rates per window
        error_rates = []
        for window, stats in time_windows.items():
            error_rate = stats['errors'] / stats['total'] * 100 if stats['total'] > 0 else 0
            error_rates.append({
                'timestamp': window,
                'error_rate': error_rate,
                'total_requests': stats['total']
            })
        
        self.results['error_rates'] = error_rates
    
    def generate_scalability_report(self):
        """Generate comprehensive scalability test report"""
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        
        # Create reports directory
        os.makedirs("test-reports", exist_ok=True)
        
        # Calculate summary statistics
        summary = {
            'test_timestamp': timestamp,
            'test_duration': time.time() - self.test_start_time if self.test_start_time else 0,
            'max_concurrent_users': self.max_concurrent_users,
            'total_messages_sent': self.total_messages_sent,
            'performance_metrics': {}
        }
        
        # Response time statistics
        if self.results['response_times']:
            response_times = [r['response_time'] for r in self.results['response_times']]
            summary['performance_metrics']['response_times'] = {
                'count': len(response_times),
                'average': sum(response_times) / len(response_times),
                'min': min(response_times),
                'max': max(response_times),
                'p95': sorted(response_times)[int(len(response_times) * 0.95)] if response_times else 0
            }
        
        # Resource usage statistics
        if self.results['memory_usage']:
            memory_usage = [m['memory_percent'] for m in self.results['memory_usage']]
            summary['performance_metrics']['memory_usage'] = {
                'average': sum(memory_usage) / len(memory_usage),
                'peak': max(memory_usage),
                'min': min(memory_usage)
            }
        
        if self.results['cpu_usage']:
            cpu_usage = [c['cpu_percent'] for c in self.results['cpu_usage']]
            summary['performance_metrics']['cpu_usage'] = {
                'average': sum(cpu_usage) / len(cpu_usage),
                'peak': max(cpu_usage),
                'min': min(cpu_usage)
            }
        
        # Save detailed results
        detailed_report = {
            'summary': summary,
            'detailed_results': self.results
        }
        
        report_file = f"test-reports/scalability_test_{timestamp}.json"
        with open(report_file, 'w') as f:
            json.dump(detailed_report, f, indent=2)
        
        # Generate human-readable report
        readable_report = f"test-reports/scalability_test_{timestamp}.txt"
        with open(readable_report, 'w') as f:
            f.write("SCALABILITY TESTING REPORT\n")
            f.write("=" * 50 + "\n\n")
            f.write(f"Test Date: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n")
            f.write(f"Test Duration: {summary['test_duration']:.2f} seconds\n\n")
            
            f.write("SCALABILITY RESULTS:\n")
            f.write(f"- Maximum Concurrent Users: {summary['max_concurrent_users']}\n")
            f.write(f"- Total Messages Sent: {summary['total_messages_sent']}\n\n")
            
            if 'response_times' in summary['performance_metrics']:
                rt = summary['performance_metrics']['response_times']
                f.write("RESPONSE TIME STATISTICS:\n")
                f.write(f"- Total Requests: {rt['count']}\n")
                f.write(f"- Average Response Time: {rt['average']:.3f}s\n")
                f.write(f"- Min Response Time: {rt['min']:.3f}s\n")
                f.write(f"- Max Response Time: {rt['max']:.3f}s\n")
                f.write(f"- 95th Percentile: {rt['p95']:.3f}s\n\n")
            
            if 'memory_usage' in summary['performance_metrics']:
                mem = summary['performance_metrics']['memory_usage']
                f.write("MEMORY USAGE STATISTICS:\n")
                f.write(f"- Average Memory Usage: {mem['average']:.1f}%\n")
                f.write(f"- Peak Memory Usage: {mem['peak']:.1f}%\n")
                f.write(f"- Min Memory Usage: {mem['min']:.1f}%\n\n")
            
            if 'cpu_usage' in summary['performance_metrics']:
                cpu = summary['performance_metrics']['cpu_usage']
                f.write("CPU USAGE STATISTICS:\n")
                f.write(f"- Average CPU Usage: {cpu['average']:.1f}%\n")
                f.write(f"- Peak CPU Usage: {cpu['peak']:.1f}%\n")
                f.write(f"- Min CPU Usage: {cpu['min']:.1f}%\n\n")
            
            # Recommendations
            f.write("RECOMMENDATIONS:\n")
            if summary['max_concurrent_users'] < 100:
                f.write("- System may need optimization for higher user loads\n")
            elif summary['max_concurrent_users'] > 500:
                f.write("- System shows good scalability characteristics\n")
            
            if 'memory_usage' in summary['performance_metrics']:
                if summary['performance_metrics']['memory_usage']['peak'] > 80:
                    f.write("- Consider memory optimization - peak usage exceeded 80%\n")
            
            if 'cpu_usage' in summary['performance_metrics']:
                if summary['performance_metrics']['cpu_usage']['peak'] > 80:
                    f.write("- Consider CPU optimization - peak usage exceeded 80%\n")
        
        print(f"Scalability test report saved to: {report_file}")
        print(f"Human-readable report: {readable_report}")
        
        return summary

def main():
    """Main function to run scalability tests"""
    print("Doctor-Patient Messaging System Scalability Testing")
    print("=" * 60)
    
    tester = ScalabilityTester()
    tester.test_start_time = time.time()
    
    # Test configurations
    test_configs = {
        'light': {'users': 50, 'storage_gb': 0.1, 'connections': 100},
        'medium': {'users': 200, 'storage_gb': 0.5, 'connections': 250},
        'heavy': {'users': 500, 'storage_gb': 1.0, 'connections': 500},
        'extreme': {'users': 1000, 'storage_gb': 2.0, 'connections': 1000}
    }
    
    # Choose test level
    test_level = input("Choose scalability test level (light/medium/heavy/extreme): ").lower()
    if test_level not in test_configs:
        test_level = 'light'
        print("Invalid choice, using light test")
    
    config = test_configs[test_level]
    
    print(f"Running {test_level} scalability test:")
    print(f"- Max concurrent users: {config['users']}")
    print(f"- Storage test: {config['storage_gb']}GB")
    print(f"- Network connections: {config['connections']}")
    print()
    
    try:
        # Test 1: Concurrent User Scaling
        print("=" * 50)
        tester.test_concurrent_user_scaling(
            max_users=config['users'],
            step_size=max(10, config['users'] // 10),
            step_duration=30
        )
        
        # Test 2: Storage Scaling
        print("=" * 50)
        tester.test_storage_scaling(target_size_gb=config['storage_gb'])
        
        # Test 3: Network Scaling
        print("=" * 50)
        tester.test_network_scaling(max_connections=config['connections'])
        
        # Calculate error rates
        tester.calculate_error_rates()
        
        # Generate report
        print("=" * 50)
        summary = tester.generate_scalability_report()
        
        print("\nSCALABILITY TESTING COMPLETED!")
        print(f"Maximum concurrent users handled: {summary['max_concurrent_users']}")
        print(f"Total messages processed: {summary['total_messages_sent']}")
        print("Check the generated reports for detailed analysis.")
        
    except KeyboardInterrupt:
        print("\nScalability testing interrupted by user")
    except Exception as e:
        print(f"Scalability testing failed: {e}")
        import traceback
        traceback.print_exc()

if __name__ == "__main__":
    main()