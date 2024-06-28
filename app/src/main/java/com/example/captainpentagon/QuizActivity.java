package com.example.captainpentagon;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.graphics.drawable.GradientDrawable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuizActivity extends AppCompatActivity {

    private TextView questionTextView, questionCountTextView, scoreTextView;
    private Button option1, option2, option3, endButton;
    private List<Question> questionList;
    private Question currentQuestion;
    private int questionIndex = 0;
    private String difficulty;
    private CountDownTimer timer;
    private long timeLeftInMillis;
    private int score = 0;
    private ProgressBar progressBar;
    private long totalTimeInMillis;

    private MediaPlayer tickingSoundPlayer;
    private MediaPlayer timeUpSoundPlayer;
    private MediaPlayer correctSoundPlayer;
    private MediaPlayer wrongSoundPlayer;

    private Boolean isCheck = false;
    private Boolean isEnd = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        questionTextView = findViewById(R.id.question_text);
        questionCountTextView = findViewById(R.id.question_count);
        scoreTextView = findViewById(R.id.score_count);
        option1 = findViewById(R.id.option1);
        option2 = findViewById(R.id.option2);
        option3 = findViewById(R.id.option3);
        endButton = findViewById(R.id.end_button);
        progressBar = findViewById(R.id.progress_bar);

        difficulty = getIntent().getStringExtra("difficulty");
        questionList = getQuestionsBasedOnDifficulty(difficulty);
        Collections.shuffle(questionList);

        updateQuestionCount();
        updateScore();

        // Load the next question
        loadNextQuestion();

        option1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isCheck = true;

                checkAnswer(option1);
            }
        });

        option2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isCheck = true;

                checkAnswer(option2);
            }
        });

        option3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isCheck = true;

                checkAnswer(option3);
            }
        });

        endButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isEnd = true;
                showResult();
                if (timer != null) {
                    timer.cancel();
                }
                if (tickingSoundPlayer != null) {
                    tickingSoundPlayer.stop();
                    tickingSoundPlayer.release();
                    tickingSoundPlayer = null;
                }
                if (timeUpSoundPlayer != null) {
                    timeUpSoundPlayer.release();
                    timeUpSoundPlayer = null;
                }
                if (correctSoundPlayer != null) {
                    correctSoundPlayer.release();
                    correctSoundPlayer = null;
                }
                if (wrongSoundPlayer != null) {
                    wrongSoundPlayer.release();
                    wrongSoundPlayer = null;
                }
            }
        });

    }


    private void loadNextQuestion() {
        if (questionIndex < 10) {
            isCheck = false;
            currentQuestion = questionList.get(questionIndex);
            questionTextView.setText(currentQuestion.getQuestionText());
            option1.setText(currentQuestion.getOption1());
            option2.setText(currentQuestion.getOption2());
            option3.setText(currentQuestion.getOption3());

            option1.setClickable(true);
            option2.setClickable(true);
            option3.setClickable(true);
            questionIndex++;
            updateQuestionCount();
            resetOptions();
            startTimer();
        } else {
            showResult();
        }
    }

    private void startTimer() {
        int timeLimit = getTimeLimitBasedOnDifficulty(difficulty);
        totalTimeInMillis = timeLimit * 1000;
        timeLeftInMillis = totalTimeInMillis;

        progressBar.setMax((int) totalTimeInMillis);
        progressBar.setProgressTintList(ColorStateList.valueOf(Color.RED));

        // Initialize ticking sound
        tickingSoundPlayer = MediaPlayer.create(this, R.raw.ticking);
        tickingSoundPlayer.setLooping(true);

        timer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateCountdown();

                int progress = (int) (totalTimeInMillis - millisUntilFinished);
                progressBar.setProgress(progress);

                // Play ticking sound
                if (tickingSoundPlayer != null && !tickingSoundPlayer.isPlaying()) {
                    tickingSoundPlayer.start();
                }
            }

            @Override
            public void onFinish() {
                Toast.makeText(QuizActivity.this, "Time's up!", Toast.LENGTH_SHORT).show();
                showCorrectAnswer();
//                tickingSoundPlayer.stop();
//                tickingSoundPlayer.release();
//                tickingSoundPlayer = null;

                if (tickingSoundPlayer != null && tickingSoundPlayer.isPlaying()) {
                    tickingSoundPlayer.stop();
                    tickingSoundPlayer.release();
                    tickingSoundPlayer = null;

//            tickingSoundPlayer.prepareAsync();
                }


                // Play time-up sound
                timeUpSoundPlayer = MediaPlayer.create(QuizActivity.this, R.raw.time_up);
                timeUpSoundPlayer.start();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadNextQuestion();
                        timeUpSoundPlayer.release();
                    }
                }, 2000);
            }
        }.start();
    }

    private void updateQuestionCount() {
        questionCountTextView.setText("Question: " + questionIndex + "/10");
    }

    private void updateScore() {
        scoreTextView.setText("Score: " + score);
    }

    private void updateCountdown() {
        int secondsLeft = (int) (timeLeftInMillis / 1000);
        // Update the countdown text view here if needed
    }

    @SuppressLint("ResourceType")
    private void resetOptions() {

        option1.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        option2.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        option3.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));


    }

    private int getTimeLimitBasedOnDifficulty(String difficulty) {
        switch (difficulty) {
            case "easy":
                return 15;
            case "medium":
                return 10;
            case "hard":
                return 5;
            default:
                return 10;
        }
    }

    private void checkAnswer(Button selectedOption) {

        option1.setClickable(false);
        option2.setClickable(false);
        option3.setClickable(false);
        timer.cancel();
        tickingSoundPlayer.stop();
        tickingSoundPlayer.release();
        tickingSoundPlayer = null;

        String selectedAnswer = selectedOption.getText().toString();
        if (selectedAnswer.equals(currentQuestion.getCorrectAnswer())) {
            selectedOption.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
            correctSoundPlayer = MediaPlayer.create(this, R.raw.correct);
            correctSoundPlayer.start();
            score++;
        } else {
            selectedOption.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
            showCorrectAnswer();
            wrongSoundPlayer = MediaPlayer.create(this, R.raw.wrong);
            wrongSoundPlayer.start();
        }


        updateScore();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!isEnd){
                    loadNextQuestion();
                }

                if (correctSoundPlayer != null) {
                    correctSoundPlayer.release();
                }
                if (wrongSoundPlayer != null) {
                    wrongSoundPlayer.release();
                }
            }
        },2000);
    }


    private void showCorrectAnswer() {
        if (option1.getText().toString().equals(currentQuestion.getCorrectAnswer())) {
            option1.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
        } else if (option2.getText().toString().equals(currentQuestion.getCorrectAnswer())) {
            option2.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
        } else if (option3.getText().toString().equals(currentQuestion.getCorrectAnswer())) {
            option3.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
        }
    }



    private List<Question> getQuestionsBasedOnDifficulty(String difficulty) {
        List<Question> questions = new ArrayList<>();

        if (difficulty.equals("easy")) {
            questions.add(new Question("How does a firewall work?", "Tracks down viruses", "Filters internet data", "Ignites malware", "Filters internet data"));
            questions.add(new Question("What does timebomb malware do?", "Ticks when infecting a computer", "Creates a clock pop-up", "Activates on certain day", "Activates on certain day"));
            questions.add(new Question("Which of the following is not a type of malware?", "Ransomeware", "Catflap", "Rootkit", "Catflap"));
            questions.add(new Question("Why should you check your firewall regularly?", "All firewalls have a limited lifespan", "Firewalls are set to uninstall themselves", "Some malware can switch off a firewall", "Some malware can switch off a firewall"));
            questions.add(new Question("Which of the following is not a method for malware to spread?", "Internet", "Network", "Audio jacks", "Audio jacks"));
            questions.add(new Question("What is the primary purpose of antivirus software?", "Delete system files", "Slow down the computer", "Identify and remove malware", "Identify and remove malware"));
            questions.add(new Question("What is the main characteristic of a worm?", "Requires a host program", "Self-replicating", "Only affects hardware", "Self-replicating"));
            questions.add(new Question("Which malware can disguise itself as legitimate software?", "Ransomware", "Worm", "Trojan horse", "Trojan horse"));
            questions.add(new Question("What type of malware holds your data hostage?", "Adware", "Spyware", "Ransomware", "Ransomware"));
            questions.add(new Question("What is the primary function of spyware?", "Show advertisements", "Monitor user activity", "Destroy files", "Monitor user activity"));
            questions.add(new Question("Which type of malware displays unwanted advertisements?", "Spyware", "Worm", "Adware", "Adware"));
            questions.add(new Question("What does a rootkit do?", "Grants unauthorized access", "Displays ads", "Deletes files", "Grants unauthorized access"));
            questions.add(new Question("What is a botnet?", "A single malware", "A security software", "Network of infected computers", "Network of infected computers"));
            questions.add(new Question("What is phishing?", "Legitimate emails", "Computer game", "Scam to steal information", "Scam to steal information"));
            questions.add(new Question("What does keylogging malware do?", "Displays pop-ups", "Records keystrokes", "Infects hardware", "Records keystrokes"));
            questions.add(new Question("What type of malware creates backdoors for unauthorized access?", "Virus", "Adware", "Backdoor Trojan", "Backdoor Trojan"));
            questions.add(new Question("What is the purpose of a DDoS attack?", "Protect servers", "Fix network issues", "Overwhelm a server", "Overwhelm a server"));
            questions.add(new Question("What is a zero-day exploit?", "An old malware", "Antivirus software", "Newly discovered vulnerability", "Newly discovered vulnerability"));
            questions.add(new Question("What does ransomware demand from victims?", "Feedback", "Hardware", "Money", "Money"));
            questions.add(new Question("What is a common sign of malware infection?", "Faster performance", "No internet access", "Frequent crashes", "Frequent crashes"));
            questions.add(new Question("Which of these is a type of social engineering attack?", "DDoS", "Brute force", "Phishing", "Phishing"));
            questions.add(new Question("What is the main goal of adware?", "Delete files", "Steal passwords", "Display advertisements", "Display advertisements"));
            questions.add(new Question("What is the role of a security patch?", "Introduce new features", "Slow down the system", "Fix vulnerabilities", "Fix vulnerabilities"));
            questions.add(new Question("What does a macro virus infect?", "Operating system", "Network", "Files and documents", "Files and documents"));
            questions.add(new Question("What is the main target of ransomware?", "Hardware components", "Network configuration", "Personal files and data", "Personal files and data"));
            questions.add(new Question("Which malware can remotely control your computer?", "Spyware", "Trojan horse", "Botnet", "Botnet"));
            questions.add(new Question("What does a polymorphic virus do?", "Delete files", "Display ads", "Change its code", "Change its code"));
            questions.add(new Question("What is a common method for malware distribution?", "Installing updates", "Using antivirus", "Email attachments", "Email attachments"));
            questions.add(new Question("What is the main characteristic of a rootkit?", "Visible in task manager", "Displays pop-ups", "Hides its presence", "Hides its presence"));
            questions.add(new Question("What is the main function of a computer virus?", "Heal system files", "Protect data", "Replicate and spread", "Replicate and spread"));
            questions.add(new Question("Which malware type is known for encrypting files?", "Adware", "Spyware", "Ransomware", "Ransomware"));
            questions.add(new Question("How does a firewall work?", "Filters internet data", "Ignites malware", "Tracks down viruses", "Filters internet data"));
            questions.add(new Question("What does timebomb malware do?", "Activates on certain day", "Ticks when infecting a computer", "Creates a clock pop-up", "Activates on certain day"));
            questions.add(new Question("Which of the following is not a type of malware?", "Catflap", "Ransomware", "Rootkit", "Catflap"));
            questions.add(new Question("Why should you check your firewall regularly?", "Some malware can switch off a firewall", "Firewalls are set to uninstall themselves", "All firewalls have a limited lifespan", "Some malware can switch off a firewall"));
            questions.add(new Question("Which of the following is not a method for malware to spread?", "Audio jacks", "Internet", "Network", "Audio jacks"));
            questions.add(new Question("What is the primary purpose of antivirus software?", "Identify and remove malware", "Delete system files", "Slow down the computer", "Identify and remove malware"));
            questions.add(new Question("What is the main characteristic of a worm?", "Self-replicating", "Requires a host program", "Only affects hardware", "Self-replicating"));
            questions.add(new Question("Which malware can disguise itself as legitimate software?", "Trojan horse", "Worm", "Ransomware", "Trojan horse"));
            questions.add(new Question("What type of malware holds your data hostage?", "Ransomware", "Adware", "Spyware", "Ransomware"));
            questions.add(new Question("What is the primary function of spyware?", "Monitor user activity", "Show advertisements", "Destroy files", "Monitor user activity"));
            questions.add(new Question("Which type of malware displays unwanted advertisements?", "Adware", "Spyware", "Worm", "Adware"));
            questions.add(new Question("What does a rootkit do?", "Grants unauthorized access", "Displays ads", "Deletes files", "Grants unauthorized access"));
            questions.add(new Question("What is a botnet?", "Network of infected computers", "A single malware", "A security software", "Network of infected computers"));
            questions.add(new Question("What is phishing?", "Scam to steal information", "Legitimate emails", "Computer game", "Scam to steal information"));
            questions.add(new Question("What does keylogging malware do?", "Records keystrokes", "Displays pop-ups", "Infects hardware", "Records keystrokes"));
            questions.add(new Question("What type of malware creates backdoors for unauthorized access?", "Backdoor Trojan", "Virus", "Adware", "Backdoor Trojan"));
            questions.add(new Question("What is the purpose of a DDoS attack?", "Overwhelm a server", "Protect servers", "Fix network issues", "Overwhelm a server"));
            questions.add(new Question("What is a zero-day exploit?", "Newly discovered vulnerability", "An old malware", "Antivirus software", "Newly discovered vulnerability"));
            questions.add(new Question("What does ransomware demand from victims?", "Money", "Feedback", "Hardware", "Money"));
            questions.add(new Question("What is a common sign of malware infection?", "Frequent crashes", "Faster performance", "No internet access", "Frequent crashes"));
            questions.add(new Question("Which of these is a type of social engineering attack?", "Phishing", "DDoS", "Brute force", "Phishing"));
            questions.add(new Question("What is the main goal of adware?", "Display advertisements", "Delete files", "Steal passwords", "Display advertisements"));
            questions.add(new Question("What is the role of a security patch?", "Fix vulnerabilities", "Introduce new features", "Slow down the system", "Fix vulnerabilities"));
            questions.add(new Question("What does a macro virus infect?", "Documents and files", "Operating system", "Network", "Documents and files"));
            questions.add(new Question("What is the main target of ransomware?", "Personal files and data", "Hardware components", "Network configuration", "Personal files and data"));
            questions.add(new Question("Which malware can remotely control your computer?", "Botnet", "Spyware", "Trojan horse", "Botnet"));
            questions.add(new Question("What does a polymorphic virus do?", "Change its code", "Delete files", "Display ads", "Change its code"));
            questions.add(new Question("What is a common method for malware distribution?", "Email attachments", "Installing updates", "Using antivirus", "Email attachments"));
            questions.add(new Question("What is the main characteristic of a rootkit?", "Hides its presence", "Visible in task manager", "Displays pop-ups", "Hides its presence"));
            questions.add(new Question("What is the main function of a computer virus?", "Replicate and spread", "Heal system files", "Protect data", "Replicate and spread"));
            questions.add(new Question("Which malware type is known for encrypting files?", "Ransomware", "Adware", "Spyware", "Ransomware"));
            questions.add(new Question("How does a firewall work?", "Filters internet data", "Ignites malware", "Tracks down viruses", "Filters internet data"));
            questions.add(new Question("What does timebomb malware do?", "Activates on certain day", "Ticks when infecting a computer", "Creates a clock pop-up", "Activates on certain day"));
            questions.add(new Question("Which of the following is not a type of malware?", "Catflap", "Ransomware", "Rootkit", "Catflap"));
            questions.add(new Question("Why should you check your firewall regularly?", "Some malware can switch off a firewall", "Firewalls are set to uninstall themselves", "All firewalls have a limited lifespan", "Some malware can switch off a firewall"));
            questions.add(new Question("Which of the following is not a method for malware to spread?", "Audio jacks", "Internet", "Network", "Audio jacks"));
            questions.add(new Question("What is the primary purpose of antivirus software?", "Identify and remove malware", "Delete system files", "Slow down the computer", "Identify and remove malware"));
            questions.add(new Question("What is the main characteristic of a worm?", "Self-replicating", "Requires a host program", "Only affects hardware", "Self-replicating"));
            questions.add(new Question("Which malware can disguise itself as legitimate software?", "Trojan horse", "Worm", "Ransomware", "Trojan horse"));
            questions.add(new Question("What type of malware holds your data hostage?", "Ransomware", "Adware", "Spyware", "Ransomware"));
            questions.add(new Question("What is the primary function of spyware?", "Monitor user activity", "Show advertisements", "Destroy files", "Monitor user activity"));
            questions.add(new Question("Which type of malware displays unwanted advertisements?", "Adware", "Spyware", "Worm", "Adware"));
            questions.add(new Question("What does a rootkit do?", "Grants unauthorized access", "Displays ads", "Deletes files", "Grants unauthorized access"));
            questions.add(new Question("What is a botnet?", "Network of infected computers", "A single malware", "A security software", "Network of infected computers"));
            questions.add(new Question("What is phishing?", "Scam to steal information", "Legitimate emails", "Computer game", "Scam to steal information"));
            questions.add(new Question("What does keylogging malware do?", "Records keystrokes", "Displays pop-ups", "Infects hardware", "Records keystrokes"));
            questions.add(new Question("What type of malware creates backdoors for unauthorized access?", "Backdoor Trojan", "Virus", "Adware", "Backdoor Trojan"));
            questions.add(new Question("What is the purpose of a DDoS attack?", "Overwhelm a server", "Protect servers", "Fix network issues", "Overwhelm a server"));
            questions.add(new Question("What is a zero-day exploit?", "Newly discovered vulnerability", "An old malware", "Antivirus software", "Newly discovered vulnerability"));
            questions.add(new Question("What does ransomware demand from victims?", "Money", "Feedback", "Hardware", "Money"));
            questions.add(new Question("What is a common sign of malware infection?", "Frequent crashes", "Faster performance", "No internet access", "Frequent crashes"));
            questions.add(new Question("Which of these is a type of social engineering attack?", "Phishing", "DDoS", "Brute force", "Phishing"));
            questions.add(new Question("What is the main goal of adware?", "Display advertisements", "Delete files", "Steal passwords", "Display advertisements"));
            questions.add(new Question("What is the role of a security patch?", "Fix vulnerabilities", "Introduce new features", "Slow down the system", "Fix vulnerabilities"));
            questions.add(new Question("What does a macro virus infect?", "Documents and files", "Operating system", "Network", "Documents and files"));
            questions.add(new Question("What is the main target of ransomware?", "Personal files and data", "Hardware components", "Network configuration", "Personal files and data"));
            questions.add(new Question("Which malware can remotely control your computer?", "Botnet", "Spyware", "Trojan horse", "Botnet"));
            questions.add(new Question("What does a polymorphic virus do?", "Change its code", "Delete files", "Display ads", "Change its code"));
            questions.add(new Question("What is a common method for malware distribution?", "Email attachments", "Installing updates", "Using antivirus", "Email attachments"));
            questions.add(new Question("What is the main characteristic of a rootkit?", "Hides its presence", "Visible in task manager", "Displays pop-ups", "Hides its presence"));
            questions.add(new Question("What is the main function of a computer virus?", "Replicate and spread", "Heal system files", "Protect data", "Replicate and spread"));
            questions.add(new Question("Which malware type is known for encrypting files?", "Ransomware", "Adware", "Spyware", "Ransomware"));
            questions.add(new Question("How does a firewall work?", "Filters internet data", "Ignites malware", "Tracks down viruses", "Filters internet data"));
            questions.add(new Question("What does timebomb malware do?", "Activates on certain day", "Ticks when infecting a computer", "Creates a clock pop-up", "Activates on certain day"));
            questions.add(new Question("Which of the following is not a type of malware?", "Catflap", "Ransomware", "Rootkit", "Catflap"));
            questions.add(new Question("Why should you check your firewall regularly?", "Some malware can switch off a firewall", "Firewalls are set to uninstall themselves", "All firewalls have a limited lifespan", "Some malware can switch off a firewall"));
            questions.add(new Question("Which of the following is not a method for malware to spread?", "Audio jacks", "Internet", "Network", "Audio jacks"));
            questions.add(new Question("What is the primary purpose of antivirus software?", "Identify and remove malware", "Delete system files", "Slow down the computer", "Identify and remove malware"));
            questions.add(new Question("What is the main characteristic of a worm?", "Self-replicating", "Requires a host program", "Only affects hardware", "Self-replicating"));
            questions.add(new Question("Which malware can disguise itself as legitimate software?", "Trojan horse", "Worm", "Ransomware", "Trojan horse"));
            questions.add(new Question("What type of malware holds your data hostage?", "Ransomware", "Adware", "Spyware", "Ransomware"));
            questions.add(new Question("What is the primary function of spyware?", "Monitor user activity", "Show advertisements", "Destroy files", "Monitor user activity"));
            questions.add(new Question("Which type of malware displays unwanted advertisements?", "Adware", "Spyware", "Worm", "Adware"));
            questions.add(new Question("What does a rootkit do?", "Grants unauthorized access", "Displays ads", "Deletes files", "Grants unauthorized access"));
            questions.add(new Question("What is a botnet?", "Network of infected computers", "A single malware", "A security software", "Network of infected computers"));
            questions.add(new Question("What is phishing?", "Scam to steal information", "Legitimate emails", "Computer game", "Scam to steal information"));
            questions.add(new Question("What does keylogging malware do?", "Records keystrokes", "Displays pop-ups", "Infects hardware", "Records keystrokes"));
            questions.add(new Question("What type of malware creates backdoors for unauthorized access?", "Backdoor Trojan", "Virus", "Adware", "Backdoor Trojan"));
            questions.add(new Question("What is the purpose of a DDoS attack?", "Overwhelm a server", "Protect servers", "Fix network issues", "Overwhelm a server"));
            questions.add(new Question("What is a zero-day exploit?", "Newly discovered vulnerability", "An old malware", "Antivirus software", "Newly discovered vulnerability"));
            questions.add(new Question("What does ransomware demand from victims?", "Money", "Feedback", "Hardware", "Money"));
            questions.add(new Question("What is a common sign of malware infection?", "Frequent crashes", "Faster performance", "No internet access", "Frequent crashes"));
            questions.add(new Question("Which of these is a type of social engineering attack?", "Phishing", "DDoS", "Brute force", "Phishing"));
            questions.add(new Question("What is the main goal of adware?", "Display advertisements", "Delete files", "Steal passwords", "Display advertisements"));
            questions.add(new Question("What is the role of a security patch?", "Fix vulnerabilities", "Introduce new features", "Slow down the system", "Fix vulnerabilities"));
            questions.add(new Question("What does a macro virus infect?", "Documents and files", "Operating system", "Network", "Documents and files"));
            questions.add(new Question("What is the main target of ransomware?", "Personal files and data", "Hardware components", "Network configuration", "Personal files and data"));
            questions.add(new Question("Which malware can remotely control your computer?", "Botnet", "Spyware", "Trojan horse", "Botnet"));
            questions.add(new Question("What does a polymorphic virus do?", "Change its code", "Delete files", "Display ads", "Change its code"));
            questions.add(new Question("What is a common method for malware distribution?", "Email attachments", "Installing updates", "Using antivirus", "Email attachments"));
            questions.add(new Question("What is the main characteristic of a rootkit?", "Hides its presence", "Visible in task manager", "Displays pop-ups", "Hides its presence"));
            questions.add(new Question("What is the main function of a computer virus?", "Replicate and spread", "Heal system files", "Protect data", "Replicate and spread"));
            questions.add(new Question("Which malware type is known for encrypting files?", "Ransomware", "Adware", "Spyware", "Ransomware"));
        }

        else if (difficulty.equals("normal")) {
            questions.add(new Question("This robot malware can perform a variety of automated tasks initiated by a master computer", "Phisher", "Spyware", "Botnet", "Botnet"));
            questions.add(new Question("An acronym used for unwanted programs such as Trojans, spyware, and other malware", "UNP", "UPU", "PUP", "PUP"));
            questions.add(new Question("A malicious attack on a network used to prevent legitimate uses of network services", "BOT", "POP", "DoS", "DoS"));
            questions.add(new Question("Software which is specifically designed to disrupt or damage a computer system is called?", "Maldo", "Macro", "Malware", "Malware"));
            questions.add(new Question("A malware that can hide itself to gain access to your computer is called?", "Worm", "Spyware", "Trojan", "Trojan"));
            questions.add(new Question("What type of malware is designed to replicate and spread across networks?", "Trojan", "Virus", "Worm", "Worm"));
            questions.add(new Question("What is a backdoor in terms of computer security?", "An antivirus tool", "A firewall feature", "A hidden entry point", "A hidden entry point"));
            questions.add(new Question("Which malware is known for creating a network of zombie computers?", "Adware", "Spyware", "Botnet", "Botnet"));
            questions.add(new Question("What is a common use of a keylogger?", "To block viruses", "To defrag a hard drive", "To capture keystrokes", "To capture keystrokes"));
            questions.add(new Question("Which is not a common type of malicious software?", "Basic", "Virus", "Worm", "Basic"));
            questions.add(new Question("What does a phishing attack attempt to do?", "Clean hard drives", "Defragment data", "Trick users", "Trick users"));
            questions.add(new Question("What is the main goal of ransomware?", "To delete files", "To slow down your computer", "To extort money", "To extort money"));
            questions.add(new Question("What is the term for malware that threatens to expose or publish data unless a ransom is paid?", "Spyware", "Phishing", "Ransomware", "Ransomware"));
            questions.add(new Question("What type of malware often arrives as an email attachment?", "Worm", "Rootkit", "Trojan horse", "Trojan horse"));
            questions.add(new Question("What is a rootkit?", "An antivirus program", "A hardware device", "A type of malware", "A type of malware"));
            questions.add(new Question("What is the main characteristic of spyware?", "Deleting files", "Monitoring activities", "Spreading via networks", "Monitoring activities"));
            questions.add(new Question("What is a common characteristic of a virus?", "It hides itself", "It replicates itself", "It repairs damaged files", "It replicates itself"));
            questions.add(new Question("Which type of malware often pretends to be a legitimate software program?", "Ransomware", "Worm", "Trojan horse", "Trojan horse"));
            questions.add(new Question("What is the purpose of adware?", "Encrypting files", "Deleting system files", "Displaying advertisements", "Displaying advertisements"));
            questions.add(new Question("This robot malware can perform a variety of automated tasks initiated by a master computer", "Spyware", "Phisher", "Botnet", "Botnet"));
            questions.add(new Question("An acronym used for unwanted programs such as Trojans, spyware, and other malware", "UNP", "UPU", "PUP", "PUP"));
            questions.add(new Question("A malicious attack on a network used to prevent legitimate uses of network services", "POP", "BOT", "DoS", "DoS"));
            questions.add(new Question("What type of malware allows attackers to control your device?", "Adware", "Remote Access Trojan", "Spyware", "Remote Access Trojan"));
            questions.add(new Question("Which malware type is known for recording keystrokes?", "Ransomware", "Adware", "Keylogger", "Keylogger"));
            questions.add(new Question("What is a characteristic of a fileless malware?", "Visible in task manager", "Does not rely on files", "Replicates rapidly", "Does not rely on files"));
            questions.add(new Question("What is the primary purpose of phishing?", "Encrypt data", "Delete system files", "Steal sensitive information", "Steal sensitive information"));
            questions.add(new Question("What type of malware spreads without any user action?", "Adware", "Spyware", "Worm", "Worm"));
            questions.add(new Question("What is a common method used by malware to disguise itself?", "Clear naming", "Encryption", "Code obfuscation", "Code obfuscation"));
            questions.add(new Question("Which type of malware hijacks your browser?", "Ransomware", "Spyware", "Browser hijacker", "Browser hijacker"));
            questions.add(new Question("What is the purpose of a rootkit?", "Delete files", "Hide malware", "Display ads", "Hide malware"));
            questions.add(new Question("Which malware type is known for exploiting vulnerabilities?", "Keylogger", "Exploit kit", "Adware", "Exploit kit"));
            questions.add(new Question("What is a common symptom of a malware infection?", "Improved graphics", "Increased storage space", "Slow computer performance", "Slow computer performance"));
            questions.add(new Question("What is the primary goal of ransomware?", "Delete system files", "Demand payment", "Improve performance", "Demand payment"));
            questions.add(new Question("What type of malware is designed to spread across networks?", "Trojan horse", "Adware", "Network worm", "Network worm"));
            questions.add(new Question("Which type of malware can self-replicate?", "Spyware", "Ransomware", "Worm", "Worm"));
            questions.add(new Question("What does a rootkit do?", "Displays ads", "Hides its presence", "Deletes files", "Hides its presence"));
            questions.add(new Question("What is the primary function of adware?", "Encrypt files", "Monitor activity", "Display advertisements", "Display advertisements"));
            questions.add(new Question("What type of malware records user activity?", "Ransomware", "Adware", "Spyware", "Spyware"));
            questions.add(new Question("Which of the following is a characteristic of a trojan horse?", "Self-replicates", "Displays ads", "Disguises as legitimate software", "Disguises as legitimate software"));
            questions.add(new Question("What is the purpose of a keylogger?", "Encrypt data", "Delete files", "Record keystrokes", "Record keystrokes"));
            questions.add(new Question("What type of malware holds your files hostage?", "Adware", "Spyware", "Ransomware", "Ransomware"));
            questions.add(new Question("What is the purpose of a security patch?", "Slow down system", "Enhance graphics", "Fix vulnerabilities", "Fix vulnerabilities"));
            questions.add(new Question("What type of malware creates a backdoor?", "Worm", "Adware", "Backdoor Trojan", "Backdoor Trojan"));
            questions.add(new Question("What is a common method for malware to spread?", "Opening documents", "Installing updates", "Email attachments", "Email attachments"));
            questions.add(new Question("What is the primary goal of spyware?", "Encrypt files", "Delete data", "Monitor user activity", "Monitor user activity"));
            questions.add(new Question("What is a common sign of a malware infection?", "Improved performance", "No internet access", "Frequent crashes", "Frequent crashes"));
            questions.add(new Question("What type of attack involves overwhelming a server with traffic?", "Phishing", "Adware", "DDoS", "DDoS"));
            questions.add(new Question("What is the purpose of a botnet?", "Encrypt files", "Monitor activity", "Launch attacks", "Launch attacks"));
            questions.add(new Question("Which type of malware disguises itself as legitimate software?", "Worm", "Ransomware", "Trojan horse", "Trojan horse"));
            questions.add(new Question("What is a zero-day exploit?", "Old malware", "Antivirus software", "Newly discovered vulnerability", "Newly discovered vulnerability"));
            questions.add(new Question("What does ransomware demand from victims?", "Feedback", "Hardware", "Money", "Money"));
            questions.add(new Question("What is the primary purpose of antivirus software?", "Encrypt data", "Improve internet speed", "Detect and remove malware", "Detect and remove malware"));
            questions.add(new Question("Which malware type is known for recording keystrokes?", "Ransomware", "Adware", "Keylogger", "Keylogger"));
            questions.add(new Question("What type of malware allows attackers to control your device?", "Spyware", "Adware", "Remote Access Trojan", "Remote Access Trojan"));
            questions.add(new Question("What is a characteristic of a fileless malware?", "Visible in task manager", "Replicates rapidly", "Does not rely on files", "Does not rely on files"));
            questions.add(new Question("What is the primary purpose of phishing?", "Encrypt data", "Delete system files", "Steal sensitive information", "Steal sensitive information"));
            questions.add(new Question("What type of malware spreads without any user action?", "Adware", "Spyware", "Worm", "Worm"));
            questions.add(new Question("What is a common method used by malware to disguise itself?", "Clear naming", "Encryption", "Code obfuscation", "Code obfuscation"));
            questions.add(new Question("Which type of malware hijacks your browser?", "Ransomware", "Spyware", "Browser hijacker", "Browser hijacker"));
            questions.add(new Question("What is the purpose of a rootkit?", "Delete files", "Hide malware", "Display ads", "Hide malware"));
            questions.add(new Question("Which malware type is known for exploiting vulnerabilities?", "Keylogger", "Exploit kit", "Adware", "Exploit kit"));
            questions.add(new Question("What is a common symptom of a malware infection?", "Improved graphics", "Increased storage space", "Slow computer performance", "Slow computer performance"));
            questions.add(new Question("What is the primary goal of ransomware?", "Delete system files", "Demand payment", "Improve performance", "Demand payment"));
            questions.add(new Question("What type of malware is designed to spread across networks?", "Trojan horse", "Adware", "Network worm", "Network worm"));
            questions.add(new Question("Which type of malware can self-replicate?", "Spyware", "Ransomware", "Worm", "Worm"));
            questions.add(new Question("What does a rootkit do?", "Displays ads", "Hides its presence", "Deletes files", "Hides its presence"));
            questions.add(new Question("What is the primary function of adware?", "Encrypt files", "Monitor activity", "Display advertisements", "Display advertisements"));
            questions.add(new Question("What type of malware records user activity?", "Ransomware", "Adware", "Spyware", "Spyware"));
            questions.add(new Question("Which of the following is a characteristic of a trojan horse?", "Self-replicates", "Displays ads", "Disguises as legitimate software", "Disguises as legitimate software"));
            questions.add(new Question("What is the purpose of a keylogger?", "Encrypt data", "Delete files", "Record keystrokes", "Record keystrokes"));
            questions.add(new Question("What type of malware holds your files hostage?", "Adware", "Spyware", "Ransomware", "Ransomware"));
            questions.add(new Question("What is the purpose of a security patch?", "Slow down system", "Enhance graphics", "Fix vulnerabilities", "Fix vulnerabilities"));
            questions.add(new Question("What type of malware creates a backdoor?", "Worm", "Adware", "Backdoor Trojan", "Backdoor Trojan"));
            questions.add(new Question("What is a common method for malware to spread?", "Opening documents", "Installing updates", "Email attachments", "Email attachments"));
            questions.add(new Question("What is the primary goal of spyware?", "Encrypt files", "Delete data", "Monitor user activity", "Monitor user activity"));
            questions.add(new Question("What is a common sign of a malware infection?", "Improved performance", "No internet access", "Frequent crashes", "Frequent crashes"));
            questions.add(new Question("What type of attack involves overwhelming a server with traffic?", "Phishing", "Adware", "DDoS", "DDoS"));
            questions.add(new Question("What is the purpose of a botnet?", "Encrypt files", "Monitor activity", "Launch attacks", "Launch attacks"));
            questions.add(new Question("Which type of malware disguises itself as legitimate software?", "Worm", "Ransomware", "Trojan horse", "Trojan horse"));
            questions.add(new Question("What is a zero-day exploit?", "Old malware", "Antivirus software", "Newly discovered vulnerability", "Newly discovered vulnerability"));
            questions.add(new Question("What does ransomware demand from victims?", "Feedback", "Hardware", "Money", "Money"));
            questions.add(new Question("What is the primary purpose of antivirus software?", "Encrypt data", "Improve internet speed", "Detect and remove malware", "Detect and remove malware"));
        }

        else if (difficulty.equals("hard")) {
            questions.add(new Question("A technique where the malware changes its appearance to avoid detection", "Phishing", "Polymorphism", "Spoofing", "Polymorphism"));
            questions.add(new Question("A program that disguises itself as a legitimate software but performs malicious activities", "Spyware", "Adware", "Trojan horse", "Trojan horse"));
            questions.add(new Question("A software vulnerability that is exploited before it is known to the vendor", "DDoS", "Zero-day exploit", "Phishing", "Zero-day exploit"));
            questions.add(new Question("A malware that records keystrokes to steal information", "Ransomware", "Spyware", "Keylogger", "Keylogger"));
            questions.add(new Question("An attack where the attacker sends fake emails to steal sensitive information", "Spoofing", "Phishing", "DDoS", "Phishing"));
            questions.add(new Question("A malware that changes its code to avoid detection", "Adware", "Polymorphic virus", "Spyware", "Polymorphic virus"));
            questions.add(new Question("A software designed to monitor and record user's activities", "Ransomware", "Spyware", "Adware", "Spyware"));
            questions.add(new Question("An attack where the attacker pretends to be another device or user", "Phishing", "Spoofing", "DDoS", "Spoofing"));
            questions.add(new Question("A malware that hides itself to avoid detection", "Adware", "Rootkit", "Keylogger", "Rootkit"));
            questions.add(new Question("A software used to protect a computer from malware", "Ransomware", "Antivirus", "Spyware", "Antivirus"));
            questions.add(new Question("A technique where the malware exploits a vulnerability before it is known", "Phishing", "Zero-day exploit", "DDoS", "Zero-day exploit"));
            questions.add(new Question("A malware that encrypts files and demands a ransom", "Adware", "Ransomware", "Spyware", "Ransomware"));
            questions.add(new Question("An attack that makes a server unavailable by overwhelming it with traffic", "Phishing", "DDoS", "Spoofing", "DDoS"));
            questions.add(new Question("A technique where the malware changes its appearance to avoid detection", "Polymorphism", "Phishing", "Spoofing", "Polymorphism"));
            questions.add(new Question("A malicious program that disguises itself as legitimate software", "Spyware", "Adware", "Trojan horse", "Trojan horse"));
            questions.add(new Question("A software vulnerability that is unknown to the vendor", "Phishing", "Zero-day exploit", "DDoS", "Zero-day exploit"));
            questions.add(new Question("A malware that records keystrokes to steal information", "Ransomware", "Spyware", "Keylogger", "Keylogger"));
            questions.add(new Question("An attack where the attacker sends fake emails to steal information", "Phishing", "DDoS", "Spoofing", "Phishing"));
            questions.add(new Question("A malware that changes its code to avoid detection", "Adware", "Polymorphic virus", "Spyware", "Polymorphic virus"));
            questions.add(new Question("A program designed to monitor and record user activities", "Ransomware", "Spyware", "Adware", "Spyware"));
            questions.add(new Question("An attack where the attacker pretends to be another device or user", "Phishing", "Spoofing", "DDoS", "Spoofing"));
            questions.add(new Question("A malware that hides itself to avoid detection", "Adware", "Rootkit", "Keylogger", "Rootkit"));
            questions.add(new Question("A software used to protect a computer from malware", "Ransomware", "Antivirus", "Spyware", "Antivirus"));
            questions.add(new Question("A technique where the malware exploits a vulnerability before it is known", "Phishing", "Zero-day exploit", "DDoS", "Zero-day exploit"));
            questions.add(new Question("A malware that encrypts files and demands a ransom", "Adware", "Ransomware", "Spyware", "Ransomware"));
            questions.add(new Question("An attack that makes a server unavailable by overwhelming it with traffic", "Phishing", "DDoS", "Spoofing","DDoS"));
            questions.add(new Question("What is a botnet?", "A network of infected computers", "A type of antivirus software", "A secure network connection", "A network of infected computers"));
            questions.add(new Question("What does a DDoS attack attempt to do?", "Overwhelm a server with traffic", "Encrypt user data", "Delete system files", "Overwhelm a server with traffic"));
            questions.add(new Question("What is the primary purpose of antivirus software?", "To detect and remove malware", "To encrypt files", "To increase computer speed", "To detect and remove malware"));
            questions.add(new Question("What is the term for a type of malware that demands payment in return for access to files?", "Ransomware", "Spyware", "Adware", "Ransomware"));
            questions.add(new Question("What does a worm do?", "Replicates and spreads across networks", "Encrypts files", "Deletes system files", "Replicates and spreads across networks"));
            questions.add(new Question("What is a Trojan horse?", "Malware disguised as legitimate software", "A secure network connection", "A type of antivirus software", "Malware disguised as legitimate software"));
            questions.add(new Question("What is a buffer overflow?", "Exploiting a program's memory", "A type of DDoS attack", "Encrypting data", "Exploiting a program's memory"));
            questions.add(new Question("What does a rootkit do?", "Provides unauthorized access to a system", "Blocks advertisements", "Deletes user files", "Provides unauthorized access to a system"));
            questions.add(new Question("What is a logic bomb?", "Malicious code that executes based on a trigger", "A virus that spreads rapidly", "An exploit used to steal passwords", "Malicious code that executes based on a trigger"));
            questions.add(new Question("What is a buffer overflow attack?", "Exploiting a program's memory", "A type of phishing attack", "Encrypting user data", "Exploiting a program's memory"));
            questions.add(new Question("What is the main goal of social engineering?", "Manipulating people to gain information", "Deleting files on a computer", "Encrypting user data", "Manipulating people to gain information"));
            questions.add(new Question("What is a typical goal of a DoS attack?", "To disrupt network services", "To secure a network", "To encrypt user data", "To disrupt network services"));
            questions.add(new Question("What is a backdoor in cybersecurity?", "An undocumented entry point", "A secure network connection", "A type of malware", "An undocumented entry point"));
            questions.add(new Question("The technique of stealing personal information through fake emails is called", "Phishing", "Spamming", "Spoofing", "Phishing"));
            questions.add(new Question("A malicious program that locks your data until a ransom is paid is called", "Ransomware", "Adware", "Spyware", "Ransomware"));
            questions.add(new Question("An advanced form of malware that can evade traditional antivirus detection", "APT (Advanced Persistent Threat)", "Adware", "Keylogger", "APT (Advanced Persistent Threat)"));
            questions.add(new Question("A form of malware that collects information about users without their knowledge", "Spyware", "Ransomware", "Adware", "Spyware"));
            questions.add(new Question("A malware that self-replicates and spreads to other devices on the network", "Worm", "Trojan horse", "Rootkit", "Worm"));
            questions.add(new Question("A security measure designed to protect a network from unauthorized access", "Firewall", "Adware", "Keylogger", "Firewall"));
            questions.add(new Question("An attack that aims to make a machine or network resource unavailable to its intended users", "DoS (Denial of Service)", "APT", "Ransomware", "DoS (Denial of Service)"));
            questions.add(new Question("A program that appears legitimate but performs malicious activities", "Trojan horse", "Spyware", "Adware", "Trojan horse"));
            questions.add(new Question("A malicious software that secretly monitors user's activity and sends the information to the attacker", "Spyware", "Adware", "Ransomware", "Spyware"));
            questions.add(new Question("A software vulnerability that is unknown to the vendor", "Zero-day exploit", "Trojan horse", "Rootkit", "Zero-day exploit"));
            questions.add(new Question("A type of attack where multiple compromised systems attack a single target", "DDoS (Distributed Denial of Service)", "Phishing", "Spoofing", "DDoS (Distributed Denial of Service)"));
            questions.add(new Question("A method used by malware to avoid detection", "Code obfuscation", "Phishing", "Encryption", "Code obfuscation"));
            questions.add(new Question("A malware designed to provide unauthorized access to a computer or network", "Backdoor", "Adware", "Spyware", "Backdoor"));
            questions.add(new Question("A program that logs keystrokes to steal sensitive information", "Keylogger", "Adware", "Ransomware", "Keylogger"));
            questions.add(new Question("An attack that involves sending fraudulent emails to steal sensitive information", "Phishing", "DDoS", "APT", "Phishing"));
            questions.add(new Question("A malware that alters its code to avoid detection", "Polymorphic virus", "Spyware", "Ransomware", "Polymorphic virus"));
            questions.add(new Question("A program that secretly records all activities on a computer", "Spyware", "Adware", "Trojan horse", "Spyware"));
            questions.add(new Question("An attack where the attacker impersonates another device or user on a network", "Spoofing", "Phishing", "DDoS", "Spoofing"));
            questions.add(new Question("A malicious program that hides itself to avoid detection", "Rootkit", "Adware", "Keylogger", "Rootkit"));
            questions.add(new Question("A software used to prevent, detect, and remove malicious software", "Antivirus", "Ransomware", "Spyware", "Antivirus"));
            questions.add(new Question("An attack that exploits a vulnerability before it is known", "Zero-day exploit", "Spoofing", "DDoS", "Zero-day exploit"));
            questions.add(new Question("A malware that encrypts files and demands a ransom for decryption", "Ransomware", "Adware", "Spyware", "Ransomware"));
            questions.add(new Question("An attack that overwhelms a server with traffic to make it unavailable", "DDoS", "Phishing", "Spoofing", "DDoS"));
            questions.add(new Question("What is the primary objective of a cyberattack?", "To breach security", "To defragment data", "To defrag a hard drive", "To breach security"));
            questions.add(new Question("What is the primary purpose of a virus?", "To replicate and spread", "To delete files", "To encrypt data", "To replicate and spread"));
            questions.add(new Question("What is a keylogger?", "A type of malware", "An antivirus program", "A hardware device", "A type of malware"));
            questions.add(new Question("What is the term for a piece of software that captures and stores keystrokes?", "Keylogger", "Debugger", "Clipboard", "Keylogger"));
            questions.add(new Question("What is a common characteristic of ransomware?", "It encrypts files", "It speeds up the computer", "It blocks pop-ups", "It encrypts files"));
            questions.add(new Question("What is a characteristic of spyware?", "It monitors activities", "It repairs damaged files", "It hides itself", "It monitors activities"));
            questions.add(new Question("What is the primary purpose of a worm?", "To replicate and spread", "To delete files", "To encrypt data", "To replicate and spread"));
            questions.add(new Question("What is a backdoor in terms of computer security?", "A hidden entry point", "An antivirus program", "A firewall feature", "A hidden entry point"));
            questions.add(new Question("What is the main goal of adware?", "Displaying advertisements", "Encrypting files", "Deleting system files", "Displaying advertisements"));
            questions.add(new Question("What is the term for a program that appears to perform one function but actually does something else?", "Trojan horse", "Worm", "Rootkit", "Trojan horse"));
            questions.add(new Question("What is the main function of a computer virus?", "To replicate and spread", "To delete system files", "To increase processing speed", "To replicate and spread"));
            questions.add(new Question("What is a phishing attack?", "A scam to steal information", "A hardware issue", "A type of antivirus software", "A scam to steal information"));
            questions.add(new Question("What is the primary purpose of a botnet?", "To launch DDoS attacks", "To encrypt data", "To defragment files", "To launch DDoS attacks"));
            questions.add(new Question("What is a polymorphic virus?", "A virus that can change its code", "A virus that spreads through networks", "A virus that deletes files", "A virus that can change its code"));
            questions.add(new Question("What is the role of a firewall?", "To filter network traffic", "To display advertisements", "To scan for viruses", "To filter network traffic"));
            questions.add(new Question("What is a rootkit?", "A type of malware", "An antivirus program", "A hardware device", "A type of malware"));
            questions.add(new Question("What is a logic bomb?", "Malicious code that executes based on a trigger", "A virus that spreads rapidly", "An exploit used to steal passwords", "Malicious code that executes based on a trigger"));

        }



        return questions;
    }

    private void showResult() {
        Intent intent = new Intent(QuizActivity.this, ResultActivity.class);
        intent.putExtra("score", score);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 恢复MediaPlayer播放
        if (tickingSoundPlayer != null && !tickingSoundPlayer.isPlaying()) {
            tickingSoundPlayer.start();
        }
    }

    @Override
    protected void onPause() {
        isEnd = true;
        super.onPause();

        if (tickingSoundPlayer != null) {
            tickingSoundPlayer.stop();
            tickingSoundPlayer.release();
            tickingSoundPlayer = null;

//            tickingSoundPlayer.prepareAsync();
        }
        if (correctSoundPlayer != null) {
            correctSoundPlayer.stop();
            correctSoundPlayer.release();
            correctSoundPlayer = null;
//            correctSoundPlayer.prepareAsync();
        }
        if (wrongSoundPlayer != null) {
            wrongSoundPlayer.stop();
            wrongSoundPlayer.release();
            wrongSoundPlayer = null;
//            wrongSoundPlayer.prepareAsync();
        }
        if (timeUpSoundPlayer != null) {
            timeUpSoundPlayer.stop();
            timeUpSoundPlayer.release();
            timeUpSoundPlayer = null;
//            timeUpSoundPlayer.prepareAsync();
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {

        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to leave the quiz?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // If the user confirms, call the super method to perform the default action
//                        super.onBackPressed();
                        Intent intent = new Intent(QuizActivity.this, QuizMainActivity.class);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("No", null)
                .show();
}

}