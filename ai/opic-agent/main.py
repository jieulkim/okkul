import sys
from graph import app
from database import execute_generated_sql, get_topic_info, get_all_topic_codes
from prompts import get_content_prompt

if __name__ == "__main__":
    # 1. 입력 받기 (TARGET_CODE 제거)
    TARGET_DIFF = input("Enter TARGET_DIFF (1-6): ").strip()
    GEN_MODE = input("Enter GEN_MODE (e.g., COMBO3, RP3, AD2): ").strip()

    # 2. 모든 토픽 코드 가져오기
    print("🔍 Fetching all topic codes from DB...")
    all_topic_codes = get_all_topic_codes()
    if not all_topic_codes:
        print("❌ No topic codes found in DB.")
        sys.exit()

    print(f"📋 Found {len(all_topic_codes)} topics: {all_topic_codes[:5]}...")  # 처음 5개만 표시

    # 3. 각 토픽에 대해 루프 실행
    success_count = 0
    failure_count = 0
    total_logs = []

    for idx, TARGET_CODE in enumerate(all_topic_codes, 1):
        print(f"\n{'='*60}")
        print(f"🔄 Processing Topic {idx}/{len(all_topic_codes)}: {TARGET_CODE}")
        print(f"{'='*60}")

        # DB에서 주제 정보 조회
        topic_info = get_topic_info(TARGET_CODE)
        if not topic_info:
            print(f"❌ Error: Topic Code '{TARGET_CODE}' not found in DB. Skipping...")
            failure_count += 1
            continue

        topic_name_kr = topic_info['name']
        
        # 규칙 기반 프롬프트 생성
        rule_prompt = get_content_prompt(GEN_MODE, TARGET_DIFF, topic_name_kr)
        
        print(f"👉 Topic: {topic_name_kr} | Mode: {GEN_MODE} | Level: {TARGET_DIFF}")

        # LangGraph 초기 상태 설정
        initial_state = {
            "topic": topic_name_kr,
            "difficulty": TARGET_DIFF,
            "gen_mode": GEN_MODE,
            "content_prompt": rule_prompt,
            "retry_count": 0,
            "generated_content": None,
            "generated_sql": None,
            "validation_result": None,
            "logs": []
        }
        
        # 실행
        print("\n🤖 Agent Workflow Started...")
        final_state = app.invoke(initial_state)
        
        # 결과 처리
        if final_state["validation_result"] and final_state["validation_result"].is_valid:
            print("\n✅ [SUCCESS] Valid OPIc Set Generated!")
            
            sql = final_state["generated_sql"].sql_query
            print("-" * 50)
            print(sql)
            print("-" * 50)

            print("💾 Executing SQL...")
            if execute_generated_sql(sql):
                print("🎉 Saved to DB!")
                success_count += 1
            else:
                print("❌ DB Execution Failed.")
                failure_count += 1
                
        else:
            print("\n❌ [FAILURE] Could not generate valid set.")
            print("Generated Content:")
            if final_state.get("generated_content"):
                for q in final_state["generated_content"].questions:
                    clean_text = q.text.replace("''", "'")
                    print(f"  Order {q.order}: {clean_text}")
            else:
                print("  No content generated.")
            failure_count += 1

        # 로그 수집
        total_logs.extend(final_state.get("logs", []))

    # ========================================================
    # [NEW] 전체 요약 리포트
    # ========================================================
    print("\n" + "="*80)
    print("📊 OVERALL SUMMARY REPORT")
    print("="*80)
    print(f"Total Topics Processed: {len(all_topic_codes)}")
    print(f"Successful Generations: {success_count}")
    print(f"Failed Generations: {failure_count}")
    print(f"Success Rate: {success_count / len(all_topic_codes) * 100:.1f}%")
    print("="*80)

    # 전체 성능 리포트
    if total_logs:
        print("\n📈 PERFORMANCE REPORT (Aggregated)")
        print("-" * 80)
        print(f"{'Step':<20} | {'Avg Time(s)':<10} | {'Total Tokens':<12} | {'Avg Cost($)':<10}")
        print("-" * 80)
        
        step_stats = {}
        for log in total_logs:
            step = log['step']
            if step not in step_stats:
                step_stats[step] = {'times': [], 'tokens': [], 'costs': []}
            step_stats[step]['times'].append(log['time_sec'])
            step_stats[step]['tokens'].append(log['total_tokens'])
            step_stats[step]['costs'].append(float(log['cost_usd'].replace('$', '')))
        
        total_time = 0
        total_tokens = 0
        total_cost = 0
        
        for step, stats in step_stats.items():
            avg_time = sum(stats['times']) / len(stats['times'])
            total_tokens_step = sum(stats['tokens'])
            avg_cost = sum(stats['costs']) / len(stats['costs'])
            print(f"{step:<20} | {avg_time:<10.2f} | {total_tokens_step:<12} | ${avg_cost:<9.4f}")
            total_time += sum(stats['times'])
            total_tokens += total_tokens_step
            total_cost += sum(stats['costs'])
        
        print("-" * 80)
        print(f"{'TOTALS':<20} | {total_time:<10.2f} | {total_tokens:<12} | ${total_cost:<9.4f}")
        print("="*80 + "\n")