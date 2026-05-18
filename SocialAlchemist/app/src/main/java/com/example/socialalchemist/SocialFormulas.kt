package com.example.socialalchemist

object SocialFormulas {
    fun calculate(recipient: String?, situation: String?): String {
        // Guard check: If the user hasn't selected both fields yet
        if (recipient == null || situation == null) {
            return "Formula incomplete. Select both a Recipient and a Situation."
        }

        // The "Alchemical" Matrix: Combines the two inputs into a structured pair
        return when (recipient to situation) {
            // Boss Formulas
            "Boss" to "Apology" ->
                "Dear Manager,\n\nI sincerely apologize for the oversight on the recent task. I take full responsibility and have already implemented measures to correct it immediately."

            "Boss" to "Congratulations" ->
                "Dear Manager,\n\nCongratulations on the wonderful news regarding the project milestone! Your leadership continues to be an incredible asset to the team."

            "Boss" to "Thanks" ->
                "Dear Manager,\n\nThank you very much for taking the time to provide constructive guidance on my recent work. I appreciate your support."

            "Boss" to "Cancel Plans" ->
                "Dear Manager,\n\nDue to an unforeseen personal obligation that requires my immediate attention, I am writing to formally request to reschedule our upcoming meeting."

            // Partner Formulas
            "Partner" to "Apology" ->
                "Hey, I'm really sorry about earlier. I let my frustration get the best of me, and that wasn't fair to you. Let's talk it through tonight?"

            "Partner" to "Congratulations" ->
                "Babe, I am so incredibly proud of you! I knew all your hard work would pay off. Let's go out tonight and celebrate your huge win!"

            "Partner" to "Thanks" ->
                "Just a little reminder to say thank you for being such an amazing and supportive force in my life. I really don't know what I'd do without you."

            "Partner" to "Cancel Plans" ->
                "Hey, I'm so bummed about this, but something urgent came up and I won't be able to make it tonight. Can we please push our date back to tomorrow?"

            // Friend Formulas
            "Friend" to "Apology" ->
                "Yo! So sorry about dropping the ball earlier. I completely lost track of time. Let me make it up to you this weekend—my treat!"

            "Friend" to "Congratulations" ->
                "Let's goooo! Massive congratulations on the win, man! You absolutely deserved this one. We need to celebrate ASAP."

            "Friend" to "Thanks" ->
                "Hey, just wanted to say thanks for looking out for me the other day. Truly appreciate having you as a brother."

            "Friend" to "Cancel Plans" ->
                "Hey mate, completely exhausted from a hectic schedule today and don't think I'll be good company. Mind if we raincheck for later this week?"

            // Parent Formulas
            "Parent" to "Apology" ->
                "Hey, I am sorry if I seemed distant or dismissive earlier. Things have been a bit overwhelming, but I love you and appreciate your patience with me."

            "Parent" to "Congratulations" ->
                "Congratulations! I'm so glad to hear your exciting news. Seeing your hard work pay off is always an inspiration to me."

            "Parent" to "Thanks" ->
                "Hey! Just wanted to send a quick text to say thank you for always being there for me, checking in, and supporting me. Love you lots!"

            "Parent" to "Cancel Plans" ->
                "Hey, something urgent popped up with my university schedule today, so I won't be able to visit tonight. Will call you as soon as I'm free!"

            // Fallback default message if a formula edge case isn't strictly matched
            else -> "Linguistic grid optimized! Formula successfully computed a response for your $recipient regarding this $situation."
        }
    }
}