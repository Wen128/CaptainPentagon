package com.example.captainpentagon;

import android.content.DialogInterface;
import android.content.Intent;
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
                checkAnswer(option1);
            }
        });

        option2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer(option2);
            }
        });

        option3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer(option3);
            }
        });

        endButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                showResult();
            }
        });
    }

    private void loadNextQuestion() {
        if (questionIndex < 10) {
            currentQuestion = questionList.get(questionIndex);
            questionTextView.setText(currentQuestion.getQuestionText());
            option1.setText(currentQuestion.getOption1());
            option2.setText(currentQuestion.getOption2());
            option3.setText(currentQuestion.getOption3());

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

        progressBar.setMax(timeLimit * 1000);

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
                if (!tickingSoundPlayer.isPlaying()) {
                    tickingSoundPlayer.start();
                }
            }

            @Override
            public void onFinish() {
                Toast.makeText(QuizActivity.this, "Time's up!", Toast.LENGTH_SHORT).show();
                showCorrectAnswer();
                tickingSoundPlayer.stop();
                tickingSoundPlayer.release();

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
        timer.cancel();
        tickingSoundPlayer.stop();
        tickingSoundPlayer.release();

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
                loadNextQuestion();
                if (correctSoundPlayer != null) {
                    correctSoundPlayer.release();
                }
                if (wrongSoundPlayer != null) {
                    wrongSoundPlayer.release();
                }
            }
        }, 3000);
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

        // 根据难度添加问题
        if (difficulty.equals("easy")) {
            questions.add(new Question("How does a firewall work?", "Filters internet data", "Ignites malware", "Tracks down viruses", "Filters internet data"));
            questions.add(new Question("What does timebomb malware do?", "Activates on certain day", "Ticks when infecting a computer", "Creates a clock pop-up", "Activates on certain day"));
            questions.add(new Question("Which of the following is not a type of malware?", "Catflap", "Ransomeware", "Rootkit", "Catflap"));
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
            questions.add(new Question("What does a macro virus infect?", "Files and documents", "Operating system", "Network", "Files and documents"));
            questions.add(new Question("What is the main target of ransomware?", "Personal files and data", "Hardware components", "Network configuration", "Personal files and data"));
            questions.add(new Question("Which malware can remotely control your computer?", "Botnet", "Spyware", "Trojan horse", "Botnet"));
            questions.add(new Question("What does a polymorphic virus do?", "Change its code", "Delete files", "Display ads", "Change its code"));
            questions.add(new Question("What is a common method for malware distribution?", "Email attachments", "Installing updates", "Using antivirus", "Email attachments"));
            questions.add(new Question("What is the main characteristic of a rootkit?", "Hides its presence", "Visible in task manager", "Displays pop-ups", "Hides its presence"));
            questions.add(new Question("What is the main function of a computer virus?", "Replicate and spread", "Heal system files", "Protect data", "Replicate and spread"));
            questions.add(new Question("Which malware type is known for encrypting files?", "Ransomware", "Adware", "Spyware", "Ransomware"));
        } else if (difficulty.equals("medium")) {
            questions.add(new Question("This robot malware can perform a variety of automated tasks initiated by a master computer", "Botnet", "Spyware", "Phisher", "Botnet"));
            questions.add(new Question("An acronym used for unwanted programs such as Trojans, spyware, and other malware", "PUP", "UNP", "UPU", "PUP"));
            questions.add(new Question("A malicious attack on a network used to prevent legitimate uses of network services", "DoS", "POP", "BOT", "DoS"));
            questions.add(new Question("Software which is specifically designed to disrupt or damage a computer system is called?", "Malware", "Maldo", "Macro", "Malware"));
            questions.add(new Question("A malware that can hide itself to gain access to your computer is called?", "Trojan", "Spyware", "Worm", "Trojan"));
            questions.add(new Question("What type of malware is designed to replicate and spread across networks?", "Worm", "Virus", "Trojan", "Worm"));
            questions.add(new Question("What is a backdoor in terms of computer security?", "A hidden entry point", "An antivirus tool", "A firewall feature", "A hidden entry point"));
            questions.add(new Question("Which malware is known for creating a network of zombie computers?", "Botnet", "Adware", "Spyware", "Botnet"));
            questions.add(new Question("What is a common use of a keylogger?", "To capture keystrokes", "To block viruses", "To defrag a hard drive", "To capture keystrokes"));
            questions.add(new Question("Which is not a common type of malicious software?", "Basic", "Virus", "Worm", "Basic"));
            questions.add(new Question("What does a phishing attack attempt to do?", "Trick users", "Clean hard drives", "Defragment data", "Trick users"));
            questions.add(new Question("What is the main goal of ransomware?", "To extort money", "To delete files", "To slow down your computer", "To extort money"));
            questions.add(new Question("What is the term for malware that threatens to expose or publish data unless a ransom is paid?", "Ransomware", "Spyware", "Phishing", "Ransomware"));
            questions.add(new Question("What type of malware often arrives as an email attachment?", "Worm", "Trojan horse", "Rootkit", "Trojan horse"));
            questions.add(new Question("What is a rootkit?", "A type of malware", "An antivirus program", "A hardware device", "A type of malware"));
            questions.add(new Question("What is the main characteristic of spyware?", "Monitoring activities", "Deleting files", "Spreading via networks", "Monitoring activities"));
            questions.add(new Question("What is a common characteristic of a virus?", "It replicates itself", "It hides itself", "It repairs damaged files", "It replicates itself"));
            questions.add(new Question("Which type of malware often pretends to be a legitimate software program?", "Trojan horse", "Worm", "Ransomware", "Trojan horse"));
            questions.add(new Question("What is the purpose of adware?", "Displaying advertisements", "Encrypting files", "Deleting system files", "Displaying advertisements"));
        } else if (difficulty.equals("hard")) {
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
        }

        return questions;
    }

    private void showResult() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Quiz Completed");
        builder.setMessage("You scored " + score + " out of 10");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Finish the activity and go back to main screen or whatever is appropriate
                Intent intent = new Intent(QuizActivity.this, QuizMainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        builder.setCancelable(false);
        builder.show();
    }
}

