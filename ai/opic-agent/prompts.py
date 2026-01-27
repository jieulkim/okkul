import re
import random

# ==============================================================================
# 1. OPIc Structure Rules (Based on provided Images)
# ==============================================================================
OPIC_STRUCTURE_RULES = {
    "LV1-2": {
        "INTRO": ["Self-introduction"],
        # 이미지의 '콤보2'는 보통 2~3문제 묶음을 의미하나, 여기선 요청된 개수(2개)에 맞춤
        "COMBO2": [
            "Description (Simple, Present Tense)", 
            "Routine or Likes/Dislikes (Simple, Present Tense)"
        ],
        "COMBO3": [
            "Description (Simple)", 
            "Routine / Current Activity", 
            "Simple Past Experience"
        ],
        "RP1": ["Asking for Information (Simple)"]
    },
    "LV3-4": {
        "INTRO": ["Self-introduction"],
        "COMBO3": [
            "Description (General / Present)", 
            "Routine / Detail / Comparison (Present)", 
            "Past Experience (MUST be Past Tense)" # 이미지: 과거 경험
        ],
        "RP3": [
            "Asking for Information (3-4 questions)", # 이미지: 정보 요청
            "Suggesting Alternatives / Problem Solving", # 이미지: 대안 제시
            "Related Past Experience (Similar situation in the past)" # 이미지: 관련 과거 경험
        ],
        "RP2": [
            "Description (Situation or Object)", 
            "Asking Eva a question about the topic" # 이미지: Eva에게 질문하기
        ]
    },
    "LV5-6": {
        "INTRO": ["Self-introduction"],
        "COMBO3": [
            "Description (Advanced / Detailed)", 
            "Routine / Comparison / Detail", 
            "Past Experience (Specific & Detailed)"
        ],
        "RP3": [
            "Asking for Information (Advanced)", 
            "Suggesting Alternatives (Complicated Situation)", 
            "Related Past Experience"
        ],
        "AD2": [
            "Comparison / Description / Routine (General)", # 이미지 Q14: 비교, 묘사, 루틴 등
            "Social Issue / News / Opinion related to the topic" # 이미지 Q15: 이슈, 뉴스, 의견 등
        ]
    }
}

# ==============================================================================
# 2. Raw Few-Shot Data (유지 - 필요한 데이터가 모두 있는지 확인 필요)
# ==============================================================================
# (이전에 제공해주신 RAW_FEW_SHOT_DATA 딕셔너리를 여기에 그대로 두시면 됩니다.)
# 디버깅을 위해 일부만 예시로 적어두겠습니다. 실제 사용 시엔 님 파일의 데이터를 유지하세요.
RAW_FEW_SHOT_DATA = {
    "COMBO2_1": [
        "What kinds of things do people do in their free time?  What are some popular activities?"
        "Who do you spend your free time with?  Your parents?  Your friends?  Who else?"

        "I would like to now talk about where you live.  Can you describe your home to me?  give me a good description of your home."
        "What things do you have in your living room?  A sofa? A chair? What else?"

        "Tell me about some of the holidays in your country."
        "When are some holidays in your country?  June? July? What other months?"

        "You indicated in the survey that you go to parks with adults.  Describe one of your favorite parks.  Tell me where it is and what it looks like. Describe the park for me."
        "What do you do at the park?  Do you inline skate in the park?  Do you play baseball in the park?  What do you do in the park?"
    ],
    "COMBO2_2": [
        "You indicated in the survey that you listen to music. What kinds of music do you listen to? Who are some of your favorite musicians or composers?",
        "What days of the week do you listen to music?",
    
        "What kind of furniture do you have at home? Tell me about different types of furniture at home. Please tell me in as much detail as possible.",
        "What is your favorite piece of furniture at home and why?",
    
        "Describe a family or a friend you have. What is he or she like? What is special about that person? Give me all the details about that person.",
        "Please tell me about your family. Do you have a brother or a sister?",
    
        "You indicated in the survey that you go to parks with adults. Describe one of your favorite parks. Tell me where it is and what it looks like. Describe the park for me.",
        "What do you do at the park? Do you inline skate in the park? Do you play baseball in the park? What do you do in the park?",
    
        "You indicated in the survey that you go to cafes. What cafes or coffee shops are in your neighborhood? Which cafe do you like to go to and why? Please tell me in detail.",
        "I work at a cafe. Ask me three or four questions about the cafe and what I do there.",
    
        "I would like to talk about where you live. Can you describe your house to me? Give me a good description of your house.",
        "What is your home address?",
    
        "Please tell me about the weather at where you live. What is the weather like in each season? Which season do you like the most?",
        "How is the weather today at where you are? Is it cold? Is it warm? Talk about today's weather in detail.",
    
        "You indicated in the survey that you go to cafes. What cafes or coffee shops are in your neighborhood? Which cafe do you like to go to and why? Please tell me in detail.",
        "When do you usually go to a cafe? Do you go to a cafe on weekdays or weekends?",
    
        "Tell me about the food in your country. What are some typical dishes?",
        "What foods do you see in a grocery store? Apples? Cereals? What other things?",
    
        "You indicated in the survey that you go to parks with adults. Describe one of your favorite parks. Tell me where it is and what it looks like. Describe the park for me.",
        "I also enjoy going to the park. Ask me three to four questions about my favorite park."
  ],
    "COMBO3_3": [
        "Tell me what your favorite Pub looks like.",
        "Tell me what you usually do to plan a night out at your favorite Pub. Do you call your friends? Do you get money at the bank first? What do you do while you are there? Tell me how you plan a night out in detail.",
        "Describe what pubs were like when you first began going to them. What were they like then and how have they changed over the years?",
    
        "You indicated in the survey that you listen to music. What kinds of music do you listen to? Who are some of your favorite musicians Andor composers?",
        "When and where do you usually go to listen to music? Do you listen to radio? Do you go to concerts? Tell me about the different ways you enjoy music.",
        "When did you first become interested in music? What kinds of music did you like first? Tell me how your interest in music has developed from your childhood until today.",
    
        "Please tell me about a healthy person you know. Who is he or she? What does he or she do to keep healthy? For example does he or she eat healthy food? Please tell me about that person in detail.",
        "Tell me about a time that you or someone you know did something new to become healthier. Maybe it was playing a sport or eating healthier food. Tell me about this change in detail.",
        "People do a lot of things to try to be healthy. They might join a healthy cooking class or join a new exercise program. Tell me about something you have done to try to improve your health. Tell me why you chose this particular activity and how it all worked out.",
    
        "Tell me about the hotels in your country. What are these places like?",
        "Tell me what you typically do when you go to a hotel. What do you do first, second, etc? When do you usually stay in hotels?",
        "People often have memories of especially beautiful or interesting hotels. Tell me about a hotel that you remember for some reason. Where was it? What did it look like? Describe it for me in as much detail as possible.",
    
        "I would like to now talk about where you live. Can you describe your home to me? What does it look like? How many rooms does it have? Give me a description with lots of details.",
        "What is your normal routine at home? What things do you usually do during the weekdays? What kinds of things do you do on the weekend?",
        "There are always problems which happen in any home. Things break, projects do not go as planned, people you live with don't cooperate. Tell me about some problems or issues that have happened in your home.",
    
        "Tell me about your favorite cafe or coffee house. What does it look like? Tell me all about it.",
        "Tell me how you first found your favorite cafe or coffee house. Did a friend recommend it or did you find it on your own? What made you want to keep going to that place?",
        "Think about a time when you went to a cafe or coffee house and something unusual or unexpected happened. Maybe the cafe was closed or maybe you plan to meet a friend who could not come. What made that particular visit to the cafe or coffee house memorable? Tell me the whole story.",
    
        "You indicated in the survey that you stay at home during your vacation. Who are the people you like to see and spend time with on your vacation?",
        "Describe some of the things you like to do with the people you visit or see during your vacation.",
        "Describe exactly what you did during the last vacation that you spent at home. Give me a description from the first to the last day of all the people you saw and everything that you did.",
    
        "I would like to now talk about where you live. Can you describe your home to me? What does it look like? How many rooms does it have? Give me a description with lots of details.",
        "What is your normal routine at home? What things do you usually do during the weekdays? What kinds of things do you do on the weekend?",
        "There are always problems which happen in any home. Things can break, a project cannot go as planned, people you live with don't cooperate. Tell me about some problems or issues that would happen in your home.",

    ],
    "COMBO3_4": [
        "I would like to ask you about gatherings in your country. What do people in your country do when they get together? Where do they go? How do they normally celebrate their gatherings? Please tell me in detail.",
        "Gatherings in big cities are often different from those in small towns. What are some of the similarities and differences the gatherings in small towns and big cities?",
        "Think about your last Gathering. Who are you with? Where did you go? Please tell me about how you spent your day on your last gathering in as much detail as possible.",
    
        "I would like to know what you like most about your cell phone. Why is that? Please tell me all the details.",
        "What was your first cell phone experience like? What was the difference between the phone you used back then and the cell phone you use now?",
        "Now please tell me about a time when you had trouble using your cell phone. When was it? What happened? Please tell me about that time in as much detail as you can.",
    
        "You indicated in the survey that you are living alone. I would like to talk about where you live. Describe your house to me. What does it look like? Where is it located? Tell me about the place in detail.",
        "Think about the time you firstly moved into your house. How has your house changed since then?",
        "I want to know about a memorable experience that had occurred in your house. When was it? What happened? Why was it so memorable? Tell me about this experience in as much detail as you can.",
    
        "You indicated in the survey that you go to the parks with your friends. Please tell me about one of the parks that you usually visit. Where is it located? What does it look like? Please describe your favorite Park in detail.",
        "What do you usually do when you go to the park? What kind of activities do you do? Who do you usually go with and why?",
        "Do you remember the first time you went to a park? When was it and who were you with? What happened? Please tell me the story from the beginning to the end.",
    
        "I would like to ask you about how people in your country dress. What kind of clothes do they wear? Tell me about fashion styles in your country in as much detail as possible.",
        "Fashion styles have changed a lot over the years. Tell me about the kinds of clothes that were popular when you were younger. How are they different from what is popular now?",
        "When was the last time you went for shopping? Tell me about a time you went to buy some clothes. Where did you go and whom did you go there with? What did you buy? What was so special about that shopping experience.",
    
        "You indicated in the survey that you like listening to music. What kind of music do you listen to? Who are some of your favorite musicians or composers?",
        "How did you first get interested in music? Please compare the music that you used to listen when you were young and the music you listen today. Also how was your interest in music changed over the years?",
        "Please tell me about the time you went to listen to some live music perhaps it was at a concert or a live cafe. Who did you go there with and how did you like the music?",
    
        "Tell me about the family or friends you like to visit. Who are these people? What are they like?",
        "Tell me about the things you typically do when you visit friends or family. Do you eat, talk or play together? What are some popular activities you do when you get together?",
        "Tell me about an early memory you have of visiting friends or family. Describe your impressions for me. Where did you go? Who did you visit? What was the experience like?",
    
        "You indicated in the survey that you go to parks with adults. Tell me about the parks you like to visit. What do these Parks look like?",
        "Describe what a typical visit to a park is like for you and children. Where do you go? How would you prepare? What things do you see and do at the park? Tell me all the details.",
        "Describe the last time you went to the park. Which Park was it? When was it that you went? Tell me everything that you did and saw from the time you arrived to the time you left on that last visit to the park.",
    
        "You indicated in the survey that you listen to music. What kinds of music do you listen to? Who are some of your favorite musicians and or composers?",
        "When did you first become interested in music? What kinds of music did you like first? Tell me how your interest in music has developed from your childhood until today.",
        "Could you think back to a particularly memorable time when you heard live music? Describe that experience in detail. When was it? Where were you? Who were you with? Who did you hear? What happened and what made that performance so memorable or special?",
    
        "What are your usual habits when you are online? Do you usually share videos, do your shopping, read the news Etc? What do you usually do online?",
        "Tell me about your favorite website or type of website to visit. Why do you like it? Give me as many details as you can.",
        "When did you first become interested in surfing the internet? Tell me about your first experience or experiences surfing the internet in great detail. What were your first impressions of it? What do you remember about it?",
    
        "Tell me what cafes and coffee houses typically look like in your community.",
        "What do people usually do at cafes and coffee houses in your country? Do they eat? Do they read? Tell me what people usually do in these places.",
        "Tell me about a time you went to a cafe or a coffee house that was very memorable. Did something unusual or strange happen? Tell me all about that memorable experience.",
    
        "Tell me about the banks in your country. What do they typically look like? Where are they usually located?",
        "Banks have definitely changed over time. Tell me about a bank you remember from your childhood. What was that bank like? How was it different from Banks today?",
        "Sometimes problems arise when dealing with the bank. Tell me in detail about a problem you had that involved your bank. Maybe the bank was closed when it needed to be opened, maybe the bank made some kind of mistake. Tell me the story of a time when you had to solve a problem involving your bank."

    ],
    "COMBO3_5": [
        "I would like to ask you about the banks in your country. What do they typically look like? Where are they usually located? Please tell me about the banks in your country.",
        "What do people usually do at the bank? Why do you go to the bank, and what do you do there? Tell me everything about what happens when you visit the bank.",
        "Banks have changed a lot over the years. What were the banks like when you were little? How are they different from now? Are there any similarities? Give me all the details.",
    
        "I would like to know one of your favorite restaurants in your area. Where is it located? What does it look like? Also, what kind of food do they serve, and why do you like to visit there? Please tell me in detail.",
        "Now, tell me about a restaurant you used to go to in your childhood. What was it like? How is it different from the restaurant you go to these days? Tell me about the difference in detail.",
        "Tell me about a time you ate out at recently. What kind of restaurant did you go to? What did you eat? When was it, and whom did you go with? Did you like the food there? Tell me everything about that experience in detail.",
    
        "What kind of furniture do you have at home? Tell me about different types of furniture at home. Please tell me in as much detail as possible.",
        "Tell me about the furniture that you had in your childhood home. How are they different from the furniture you have today? Describe the difference in detail.",
        "Tell me about the time when you had problems with your furniture. Maybe it could have gotten broken or damaged, or it could have gotten ruined. Tell me what happened and how you solved the problem in as much detail as possible.",
    
        "I would like you to describe one of the countries or cities you usually visit. What does the place look like? Why do you like to visit there? How are the people like there? Please tell me in detail.",
        "Can you tell me about the things you like to do when you visit another country?",
        "Describe for me your first trip to another country. When was the trip? Where did you go? Who did you go with, and what did you do? Describe that experience to me in as much detail as possible.",
    
        "Tell me more about the weather in your country. Are there different seasons? What is the weather typically like?",
        "Has the weather in your country changed over time? What was the weather typically like when you were a child? How was the weather back then different from the weather today?",
        "Tell me about an experience you had when the weather created some kind of problem. Perhaps roads were flooded, or maybe schools and businesses were closed due to the snow or cold. Tell me about an experience you had when severe weather had created some kind of problem. Give me lots of details.",
    
        "What kind of music do you listen to? Who are some of your favorite musicians or composers?",
        "When did you first become interested in music? What kind of music did you like first? Tell me how your interest in music developed from your childhood until today.",
        "Could you think back to a particularly memorable time when you heard live music? Describe that experience in detail. When was it? Where were you? Who were you with? How did you hear it? What happened, and what made that performance memorable or special?",
    
        "Tell me what cafes and coffee houses typically look like in your community.",
        "What do people usually do at cafes and coffee houses in your country? Do they eat? Do they read? Tell me what people usually do in these places.",
        "Tell about a time you went to a cafe or coffee house that was very memorable. Did something unusual or strange happen? Tell me all about that memorable experience.",
    
        "You indicated in the survey that you stay at home for vacation. Who are the people you like to see and spend time with on your vacation?",
        "Describe exactly what you did during the last vacation that you spent at home. Give me a description, from the first to the last day, of all the people you saw and everything that you did.",
        "Could you tell me about an unusual, unexpected, or satisfying experience that you had while at home on vacation? Tell me all the details of that experience. What happened? Who was involved? Why was this experience so memorable?",
    
        "You indicated in the survey that you listen to music. What kinds of music do you listen to? Who are some of your favorite musicians and or composers?",
        "When did you first become interested in music? What kinds of music did you like first? Tell me how your interest in music developed from your childhood until today.",
        "Could you think back to a particularly memorable time when you heard live music? Describe that experience in detail. When was it? Where were you? Who were you with? Who did you hear? What happened, and what happened that made that performance so memorable or special?",
    
        "I would like to ask you where you live. Please describe your house in detail. What does it look like? How many rooms are there? Also, which room is your favorite room and why?",
        "Now, tell me how you clean your house. What do you do to maintain it? Tell me about all the things you do to keep your house clean.",
        "I want to know about a memorable experience that had occurred in your house. When was it? What happened? Why was it so memorable? Tell me about this experience in as much detail as you can.",
  ],
    "COMBO3_6": [
        "What do you like most about your phone? Maybe you like the camera or maybe you like certain applications. Tell me some of the reasons you like those features.",
        "Tell me about a recent phone call you remember. Who did you talk with and what did you talk about? What made the phone call so memorable.",
        "Tell me about a time when you had trouble using your phone. What was the problem and how did you deal with the situation? Please tell me everything in as much detail as possible.",
    
        "You indicated in the survey that you take trips in your home country. Where do you like to visit and why do you like to go there?",
        "What do people do during their trips in your country? What are some things they do in order to prepare for trips?",
        "Talk about a trip you went on in your childhood. What do you remember about that trip? Where did you go and whom did you go with? What made the trip special?",
    
        "You indicated in the survey that you watch movies. Who do you usually go to the movies with? Are there any special reasons that you watch movies with this person?",
        "I would like you to talk about one of the movies that you saw when you were a child. How has your interest in movies changed? Tell me everything about it in as much detail as possible.",
        "Have you ever been bothered by someone or something while you were watching a movie? How exactly did this person bother you? Tell me about the situation in detail.",
    
        "Describe the geography in your country. Are there mountains? Is the seashore part of the geography? Are there lakes? Discuss all the details you can about the geography and landscape in your country.",
        "What types of activities do the people in your country do for entertainment? Are they interested in different types of things than people from other countries? Provide a detailed description of the things that people enjoy doing in your country.",
        "Discuss something memorable that you experienced during your childhood. For instance, it could have been a special event or an experience you had with your parents. Describe the event and everything that happened.",
    
        "Do you have a favorite park? Describe this park by telling me what it looks like.",
        "Describe your experience the last time you went to the park in detail. Explain which park you went to, what do you did while there, and who went there with you.",
        "Explain to me the story of an experience you had while going to a park. Maybe something comical, unexpected, or interesting happened. Tell me all about that memory.",
    
        "Describe the locations where you usually take a walk. Explain what the places look like where you walk.",
        "Explain when you started walking consistently on a regular basis. Why did you start walking?",
        "Have you ever had a particularly memorable experience while walking? Where and when? Who were you with? What happened during this walk?",
    
        "You indicated in the background survey that you have lived in a dormitory. Where was it? What kind of facilities did it have? Why did you like living there?",
        "I'd like to know more about dormitory life. Please describe a memorable experience you had while living in a dormitory. Who did you stay with? What happened?",
        "There are factors you need to consider when choosing accommodation. What is the most important factor for you when selecting a place to stay? Provide as many details as possible in your response.",
    
        "Tell me about your favorite trip. Where did you go and where did you stay? Describe the place you stayed and some places you visited. Give me all the details about the trip.",
        "What is the best meal you had while traveling? Where were you and what did you eat? Why did you enjoy it? Describe the meal to me in detail.",
        "When people travel to other countries, what sorts of things are they most interested in? Why do you think these things are of such interest to travelers?",
    
        "You indicated that you watch sports games. What sport do you like to watch? Who is your favorite team or player? Why do you like to watch them?",
        "Which sport is the most popular in your country? Why do you think it is popular? Has the sport always been popular or has it gained popularity recently?",
        "Have you ever been disappointed by the outcome of a match? Did a team or a player let you down? What happened during the game? Tell me the whole story."
  ]
        
    ,
    "RP1_1": [
        "I’d like to give you a situation and ask you to act it out.  You are visiting New York on vacation.  You go to a car rental agency to rent a car.  Imagine that you are speaking to the rental car agent and ask three or four questions about renting a car for a week."
    ],
    "RP1_2": [
        "I would like to give you a situation and ask you to act it out you need to make an appointment with your doctor call the office and describe what you need then ask three or four questions to find out when the doctor is available",
        
        "I'm going to give you a situation and ask you to act it out you want to buy a new cell phone call a store and ask three to four questions about a new cell phone you want to buy"
    ],
    "RP2_1": [
        "I would like to give you a situation and ask you to act it out you need to make an appointment with your doctor call the office and describe what you need then ask three or four questions to find out when the doctor is available",
        
        "I'm going to give you a situation and ask you to act it out you want to buy a new cell phone call a store and ask three to four questions about a new cell phone you want to buy"
    ],
    "RP2_2": [
        "I would like to give you a situation and ask you to act it out you need to make an appointment with your doctor call the office and describe what you need then ask three or four questions to find out when the doctor is available",
        
        "I'm going to give you a situation and ask you to act it out you want to buy a new cell phone call a store and ask three to four questions about a new cell phone you want to buy"
    ],
    "RP2_3": [
        "What kinds of Technology do people typically use in your country? Do people use computers, cell phones, handheld devices? What are some common forms of technology that people have?",
        "I just bought a new laptop computer. Ask me three or four questions about my new computer.",
    
        "You indicated in the survey that you stay at home for vacation. Who are the people you like to see and spend time with on your vacation?",
        "Now ask me three or four questions about the kinds of things that I like to do while on vacation.",
    
        "You indicated in the survey that you go to parks with adults. Tell me about the parks you like to visit. What do the Parks look like?",
        "I enjoy going to parks in my free time. Ask me three or four questions about the park that I like to find out as much information about it as you can.",
    
        "You indicated in the survey that you go to cafes. What cafes or coffee shops are in your neighborhood? Which Cafe do you like to go to and why? Please tell me in detail.",
        "I work at a cafe. Ask me three or four questions about the cafe and what I do there."
  ]
    ,
    "RP2_4": [
        "You indicated in the survey that you like to take a walk. Where do you normally take a walk? What does the place look like and where is it located? Please describe the place you normally take a walk.",
        "You indicated in the survey that you like jogging. I also enjoy jogging. Ask me three to four questions about why I like to jog.",
        
        "What do people normally do on the internet? Do they play games, listen to music or watch movies? Please tell me about the things people do online.",
        "I also use the internet. Ask me three or four questions about the website I like to visit and also what I do on the internet.",
        
        "Tell me about the kinds of things healthy people eat. What foods are they? Where do they buy these Foods?",
        "I work hard to maintain good health. Ask me three or four questions to find out what I do.",
        
        "I'd like to talk about where you live. Tell me about your favorite room in your home. What does it look like?",
        "I've asked many questions about you now. I'd like you to ask me three or four questions about my home."
  ],
    "RP3_3": [
        "I'd like to give you a situation and ask you to act it out. You are looking for a new home. Call the real estate agent, introduce yourself and then ask three or four questions about the homes and services they offer.",
        "I'm sorry but there's the problem which I'll need you to resolve. When you get to your new home you discovered that a window is broken. Call the repair shop and leave a message. Describe in detail the condition of the window, explain how you think the window got broken, give two or three reasons why it is important that the window be fixed today.",
        "That's the end of the situation. Have you ever been surprised to discover something broken at home? Tell me the story of what you found, how it was broken and what you did to solve the problem.",
    
        "I want to visit your country. You want to help me but you need more information. Ask me three or four questions to learn more about me and my trip.",
        "I am arriving for a visit in your country today. You are going to meet me at the airport but you have a sudden emergency and cannot make it. Call my voicemail and leave a message. Explain the problem and give me two to three options so I can get from the airport to your home.",
        "Tell me in detail about a time when you learned or realized something interesting about your country. Maybe you watched a sporting event, witnessed a major storm or observed the actions of your country's government and citizens. Tell me about that experience from beginning to end.",
    
        "I would like to give you a situation and ask you to act it out. Your friend tells you about a great new app on her phone. Ask your friend three or four questions about this app on her phone.",
        "I'm sorry but there is a problem which I'll need you to resolve. After downloading the app your friend recommended you notice that you have a problem with it. Call the customer service number to explain the problem and make some suggestions on how you can resolve it.",
        "That's the end of the situation. Have you ever had a problem using a new piece of technology? Maybe there was something wrong with it or it was just not as easy to use. Tell me in detail about a problem you encountered with some new technology or a device. What did you do to solve the problem?",
    
        "I'm going to give you a situation and ask you to act it out. You would like to book a hotel for your trip. Call a hotel and ask three or four questions about that hotel.",
        "I'm sorry but there is a problem that needs to be resolved. You have checked in but the room is not cleaned up properly. Call the front desk, explain the situation and offer two to three options to resolve this matter.",
        "Think about a memorable hotel you stayed at. Where was it located? Who did you go there with? What made you visit to that hotel? Was there something interesting happened? Please tell me about that story from the beginning to the end."
  ],
    "RP3_4": [
        "I'm going to give you a situation and ask you to act it out. You have a job interview tomorrow at a company but you don't have enough information about the company. Call the company and ask some questions about that company.",
        "Unfortunately you are not able to make it to the interview since something urgent has happened to you. Call the company, explain the situation and offer two to three alternatives to handle this problem.",
        "That's the end of the situation. Have you ever had to cancel an appointment or a meeting for some reason? What exactly happened and how did you deal with the situation? Please give me all the details.",
       
        "I'm going to give you a situation and ask you to act it out. You want to visit a coffee shop that is recently opened in your area. Call the coffee shop and ask three to four questions about their menu.",
        "Unfortunately you have ordered a coffee but when you got your coffee you found out that it was the wrong coffee. Call one of the staff members and explain the situation then offer two to three alternatives to solve the problem.",
        "That's the end of the situation. Have you ever been in a situation that you had ordered something but got the wrong order? Where were you and what did you order? What exactly happened and how did you deal with the situation? Please describe the event in detail.",
        
        "I'd like to give you a situation and ask you to act it out. You want to buy a new piece of furniture for your home. At the store you see a piece that you like and have some questions about it. Pretend you are speaking with a salesperson. Ask three or four questions to get all the information you need about the furniture you like.",
        "I'm sorry there is a problem which I'll need you to resolve. When you get the piece of furniture home you don't like the way it looks. Call the store, explain why you are not satisfied and propose a few solutions to resolve the problem.",
        "That's the end of the situation. Has anything like that ever happened to you? Have you ever bought something for your home or for yourself that just did not work out? Tell me everything that happened and how you eventually solved the problem.",
        
        "I'd like to give you a situation and ask you to act it out. You are visiting New York on vacation. You go to a car rental agency to rent a car. Imagine that you are speaking to a rental car agent and ask three or four questions renting a car for a week.",
        "I'm sorry but there's a problem which I'll need you to resolve. I'm not sure if I can rent you a car because I'm not familiar with your non-US driver's license. Can you explain to me what the license says and why it is the same as the valid U.S license?",
        "That's the end of the situation. Can you now tell me the story of an experience that you had while you were on one of your vacations that was memorable because something unexpected happened or because it was so enjoyable? Start the story by telling me when this happened, where you were and who you were traveling with and then tell me all the details of the story."
  ],
    "RP3_5": [
        "I'd like to give you a situation and ask you to act it out. Your friend is having a birthday party at a bar tomorrow and you are invited to the party. Call your friend and ask three to four questions about the party.",
        "I'm sorry, but there is a problem you need to resolve. You realized that you can't go to the party because you have an important exam tomorrow and you still have a lot to study. Call your friend, explain the situation, then give two to three solutions to resolve this problem.",
        "Tell me about a memorable incident that happened at a bar. When was it? Who were you with? What exactly happened there? Was there something interesting going on? Why was it so special? Tell me everything about that day at the bar from the beginning to the end.",
    
        "I'd like to give you a situation and ask you to act it out. You are looking for a new home. Call the real estate agent, introduce yourself, and then ask three or four questions about the homes and services they offer.",
        "I'm sorry, but there's a problem which I'll need you to resolve. When you get to your new home, you discovered that a window is broken. Call the repair shop and leave a message. Describe in detail the condition of the window. Explain how you think the window got broken. Give two or three reasons why it is important that the window be fixed today.",
        "That's the end of the situation. Have you ever been surprised to discover something broken at home? Tell me the story of what you found, how it was broken, and what you did to solve the problem.",
    
        "I'd like to give you a situation and ask you to act it out. Your friend just got a new phone with lots of features. Ask your friend three or four questions about the new phone.",
        "I'm sorry, there's a problem which I'll need you to resolve. You buy a new mobile phone, but when you get home and open the box, you realize it is not the one you wanted. Call the store and explain the situation in detail.",
        "That's the end of the situation. Tell me about a problem you had with a phone one time. Maybe you dropped it in the water or you broke the screen. Explain what happened and what you had to do to either replace it or fix the problem.",
    
        "I'd like to give you a situation and ask you to act it out. You have moved into a new building and you want to know the recycling rules for this apartment. Call the manager of the building and ask three or four questions about recycling.",
        "I'm sorry, but there is a problem you need to resolve. You have a new resident from abroad in your building; however, he's been throwing away garbage in the recycling bin. Other residents are complaining about that. Go to the new resident and explain the situation and the recycling policy.",
        "I would like to ask you about an unexpected incident regarding recycling. When was it? Who were you with? What exactly happened? How did you solve the situation? Tell me everything about that day from the beginning to the end.",
    
        "I'd like to give you a situation to act out. One of your friends is a huge fan of traveling, so the friend has a lot of information about it. Call the friend to ask for help for your trip. Ask him or her about tourist attractions, vacation spots, accommodations, or budget. Also, ask your friend about websites for traveling.",
        "I'm sorry, but there is a problem I need you to resolve. You and your friends are planning to go on a trip; however, traveling has been prohibited because of an unexpected earthquake. Call your travel companions and explain the situation and give them two to three alternatives.",
        "That's the end of the situation. Have you ever canceled any plans for a trip or a party because of an unexpected incident? Why did you have to cancel it? What happened? Please tell me about any experience of calling off something unexpectedly.",
    
        "I'd like to give you a situation and ask you to act it out. One of your relatives is going on a trip. Their house is going to be empty and you are supposed to watch the house while they are on the trip. Call your relative and ask three or four questions to figure out what you should do to watch their house.",
        "I'm sorry, but there is a problem I need you to resolve. You are at your relative's house but you can't find the key to the door and you can't get into the house. Call your relative and ask two to three questions to solve the problem.",
        "Have you ever canceled any plans with your family or friends because of an unexpected incident? What was the problem? Please tell me in as much detail as possible."
  ],
    "RP3_6": [
        "I'd like to give you a situation and ask you to act it out. There is a newly opened store. Call the store and ask the clerk two or three questions to get some information.",
        "I'm sorry but there is a problem I need you to resolve. The clothes you have ordered have arrived but one of them has a problem. Call the store and explain the situation. Give two to three alternatives to solve the problem.",
        "That's the end of the situation. Do you have any experience of being unhappy with something that you bought or some service you received? What was the problem and how did you deal with the situation?",
    
        "You have decided that you want to buy an mp3 player. A friend of yours is particularly knowledgeable about them. Contact this friend and ask 3-4 questions to gain more information about purchasing an mp3 player.",
        "You borrowed an mp3 player from this friend. You unfortunately broke it. Contact your friend and describe how it broke and its current condition, then suggest two or three ways that you can get another working mp3 player for your friend.",
        "Discuss an experience where you had some sort of equipment that broke or was not working properly. Provide the background surrounding this situation and describe what happened and how did the situation become resolved.",
    
        "I'd like to give you a situation to act out. Pretend you are going to a bank to open an account. Ask the teller three or four questions about making a new account.",
        "When you got home you realized there has been a mistake with the spelling of your name. Unfortunately you cannot use a bank card with incorrect details. Call the bank manager and ask him or her to handle the issue.",
        "Have you experienced any problems at a bank? Describe a difficulty you faced and explain what happened. How did you solve this problem?"
    ],
    "AD2_5": [
        "Think about two different types of music or singers you like. What are the differences between them? How do you feel when you listen to each type of music?",
        "Let me ask you about some gadgets or equipment that people in your country are interested in when they listen to music. Why do people use them and what do people like about them? Please give me all the details.",
    
        "How do people prepare for future work in your country's Industries? Do they get a general education first? Specific training once they join a company? Do they receive specific job training from a young age? Has the process changed over the past five years? Give me all the details.",
        "What is an industry or company that people are talking about nowadays? Tell me why people are interested in this industry or company and what they are saying. Give me lots of details.",
    
        "The handling of recycling materials has changed over the years. Tell me how the collection of recycling materials used to be in the past and how it has evolved to the present.",
        "Stories about recycling are often in the media. Tell me about one news story that you have heard related to recycling or perhaps the environment. Describe what the story was about, what happened, and what the reaction was to the story. Describe these things in detail.",
    
        "Gatherings in big cities are often different from those in small towns. What are some of the similarities and differences between the gatherings in small towns and big cities?",
        "It is not easy to make plans for gatherings. What are some issues related to making plans for gatherings? How do people deal with those problems?",
    
        "Can you tell me about the changes you've heard about concerning the relationship between your country and other countries? The change might have happened in areas such as economics, sports, arts, culture, or politics. Please provide a detailed description of any observed changes.",
        "Can you provide details about a significant historical incident that has impacted the relationship between your country and one of its neighboring nations? This incident may include the signing of a treaty between two countries, a cultural event, or the visit of a foreign minister or president.",
    
        "Please compare two types of music or composers you like. Why do you enjoy those types of music or composers? What are the similarities and differences between those two different types of music?",
        "Explain some electronic gadgets or equipment that people are using to listen to music. Have you heard of any hot topics or trends regarding devices for music listening?"

    ]
        
    ,
    "AD2_6": [
        "I would like you to pick one of your neighboring countries and talk about a neighboring country that is geographically similar to your country. What are the changes the country has gone through in recent years?",
        "I would like you to tell me about an article you read about the country you have just mentioned. Describe the changes in the economic or political relationship between your country and your neighboring country.",
    
        "Many believe that traveling has become more of a struggle in the last five years. Discuss the changes you have noted while traveling. How are travelers and the travel experience in general affected by these changes?",
        "When people discuss travel, what are the main concerns that they present or focus on? How are these concerns being addressed for the future?",
    
        "I want to know about different types of industry. Tell me about the different industries that were popular in the past in your country and compare them with industries that are popular today. Are there many differences?",
        "I'd like to know about the issues within the entertainment industry. What is the biggest problem in the industry? What can be done to resolve this problem? Provide me with as many details as possible."
  ],
    "INTRO_1": [
        "Tell me about yourself."
    ]
}

# ==============================================================================
# 3. Content Generator Prompt (with Debugging)
# ==============================================================================
def get_content_prompt(gen_mode, target_diff, topic):
    print(f"\n======== [DEBUG] Prompt Generation Start ========")
    print(f"👉 Input: Mode={gen_mode}, Difficulty={target_diff}, Topic={topic}")

    diff = int(target_diff)
    
    # 1. 모드에서 목표 개수(Target Count) 추출
    count_match = re.search(r'(\d+)$', gen_mode)
    question_cnt = int(count_match.group(1)) if count_match else 2 
    print(f"👉 Parsed Question Count: {question_cnt}")

    # 2. 난이도 그룹 매핑
    if diff <= 2:
        group_key = "LV1-2"
    elif diff <= 4:
        group_key = "LV3-4"
    else:
        group_key = "LV5-6"
    print(f"👉 Mapped Difficulty Group: {group_key}")
    
    # 3. 규칙 가져오기
    # 해당 난이도 그룹에 요청한 모드(예: RP3)가 있는지 확인
    mode_rules = OPIC_STRUCTURE_RULES.get(group_key, {})
    rules = mode_rules.get(gen_mode, [])
    
    if not rules:
        print(f"⚠️ WARNING: No logic rules found for {gen_mode} in {group_key}. Using generic fallback.")
        rules = [f"General OPIc Question {i+1} related to {topic}" for i in range(question_cnt)]
    else:
        print(f"✅ Rules Found: {rules}")

    # 규칙 텍스트 생성
    rule_text = "\n".join([f"   - Order {i+1}: {r}" for i, r in enumerate(rules)])

    # 4. Few-shot 파싱 (스마트 슬라이싱 & 폴백)
    # 4-1. 우선 정확한 키(예: AD2_6)로 시도
    raw_key = f"{gen_mode}_{diff}"
    raw_list = RAW_FEW_SHOT_DATA.get(raw_key, [])
    print(f"👉 Checking Few-shot Data for Key '{raw_key}': Found {len(raw_list)} items")
    
    # 4-2. 없으면 유사 난이도로 폴백 (예: 6 -> 5)
    if not raw_list:
         fallback_diff = diff - 1 if diff % 2 == 0 else diff
         fallback_key = f"{gen_mode}_{fallback_diff}"
         raw_list = RAW_FEW_SHOT_DATA.get(fallback_key, [])
         print(f"👉 Fallback Check '{fallback_key}': Found {len(raw_list)} items")

    formatted_examples = ""
    if raw_list:
        available_sets = []
        total_items = len(raw_list)
        
        # question_cnt 단위로 데이터 자르기 (데이터 오염 방지)
        for i in range(0, total_items, question_cnt):
            chunk = raw_list[i : i + question_cnt]
            if len(chunk) == question_cnt:
                available_sets.append(chunk)
        
        print(f"👉 Valid Sets Created: {len(available_sets)} sets (Size: {question_cnt})")

        for idx, q_set in enumerate(available_sets[:2]):
            formatted_examples += f"\n[Example Set {idx+1} (Size: {question_cnt})]\n"
            for q_idx, q_text in enumerate(q_set):
                formatted_examples += f"Item {q_idx+1}: {q_text}\n"
            formatted_examples += "--- End of Set ---\n"
    else:
        print("❌ NO FEW-SHOT DATA FOUND. Zero-shot generation will be used.")
        formatted_examples = "No specific examples. Follow the strict count rules."

    print(f"======== [DEBUG] Prompt Generation End ========\n")

    # 5. 최종 프롬프트 생성
    # 모드별 특별 지침 추가 여부 결정
    mode_specific_guidelines = ""
    if not gen_mode.startswith('COMBO'):
        mode_specific_guidelines = f"""
   - **For RP3 Mode**: The three items must form a single, coherent scenario. Do not generate three independent questions. The first item should present a situation, the second should introduce a problem in that situation, and the third should ask about a related past experience. Maintain the narrative connection between the parts. **Critical: Start the first item with a situation opener like "I'd like to give you a situation and ask you to act it out." or "Imagine you are in a situation where...". Do not start with general description openers like "I'd like to know..." or "Tell me about...". For the first item, end with instructing the user to ask questions, e.g., "Call ... and ask three or four questions about ...". Do not list the questions directly. Vary the phrasing and structure to match the diversity in the examples, creating different types of scenarios and problems.**
   - **For RP2 Mode**: The first item should be a description of a situation or object related to the topic. The second item should involve asking questions about the topic, mimicking the example's style of directing the user to ask questions. **Critical: The second item should start with something like "Ask me three or four questions about..." or direct the user to ask questions. Vary the description style and question-directing phrases based on the examples to ensure diversity.**
   - **For AD2 Mode**: The first item should focus on comparison, description, or routine related to the topic. The second item should address a social issue, news, or opinion related to the topic. Ensure they are distinct and follow the example's analytical or opinion-based style. **Critical: The second item should address issues, news, or opinions, not just descriptions. Vary the analytical approaches and opinion expressions to reflect the variety in the examples.**"""

    prompt = f"""
You are an OPIc Question Generator.

### CONFIGURATION
- **Topic**: {topic}
- **Mode**: {gen_mode}
- **Required List Length**: {question_cnt} (Exactly {question_cnt} items)
- **Difficulty**: {diff}

### STRICT ORDER RULES (Logic):
{rule_text}

### FEW-SHOT EXAMPLES (Style & Length Reference):
{formatted_examples}

### 🎨 STYLE TRANSFER GUIDELINES (MIMIC THIS):
1. **Sentence Flow & Structure**:
   - **Copy the Rhythm**: If the example uses three short questions in a row (e.g., "Where is it? Who goes there? Why?"), you MUST do the same for '{topic}'.
   - **Run-on Sentences**: If the example uses long sentences connected by "and" or "so" without complex grammar, mimic that flow.
   - **Opening Phrases**: Use the same openers as the examples (e.g., "I would like to know...", "Tell me about...", "I'd like to give you a situation...").{mode_specific_guidelines}

2. **Vocabulary Level**:
   - Use **Spoken English**. Avoid academic or formal words.
   - Use "OPIc-style" phrases: "Give me all the details", "Tell me everything about it", "from beginning to end".

3. **Format**:
   - **Lowercase Start**: Start every sentence with a lowercase letter (unless it's a proper noun).
   - **Natural Punctuation**: Use periods and question marks naturally.

### 🚫 CRITICAL WARNING (TOPIC ADAPTATION):
- **DO NOT COPY THE CONTENT**: The examples might be about 'Banks' or 'Furniture'. **Ignore the subject matter.**
- **ONLY COPY THE STRUCTURE**: Apply the sentence structure of the example to the topic **'{topic}'**.
  - *Bad Example*: (Topic: Cooking) "Tell me about the banks in your country." (Copied content ❌)
  - *Good Example*: (Topic: Cooking) "Tell me about the cooking styles in your country." (Copied structure ✅)

### INSTRUCTIONS:
1. Generate exactly **{question_cnt}** items.
2. Follow the **Strict Order Rules** for the logical flow (e.g., Past Experience).
3. Output strictly in JSON format with keys: "order", "text".
"""
    return prompt

# ==============================================================================
# 4. Validator Prompt (유지)
# ==============================================================================
VALIDATOR_SYSTEM_PROMPT = """
You are a 'Mechanical' Validator. 
Your ONLY job is to count the number of question items in the list.

### RULE:
- **COMBO3, RP3**: Order count must be **3**.
- **COMBO2, RP2, AD2**: Order count must be **2**.
- **RP1, INTRO**: Order count must be **1**.

### GUIDELINES:
1. **Multiple Sentences OK**: A single question item can contain 2-3 sentences. This counts as **ONE** item.
2. **Count Items**: Count the number of items labeled as 'Order X:' in the list. Each 'Order X:' is one item, regardless of how many sentences it contains.
3. **Ignore Content**: Do not check grammar, topic, or logic.
4. **ONLY CHECK THE ORDER COUNT**.

Response Format:
If order count matches the Rule: return is_valid=True, feedback="Perfect".
If order count does NOT match: return is_valid=False, feedback="Count Mismatch".
""" 