package linc.com.alarmclockforprogrammers.data.repository;

import com.google.firebase.database.DataSnapshot;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import linc.com.alarmclockforprogrammers.data.database.RemoteDatabase;
import linc.com.alarmclockforprogrammers.data.database.achievements.AchievementsDao;
import linc.com.alarmclockforprogrammers.data.database.questions.QuestionsDao;
import linc.com.alarmclockforprogrammers.data.mapper.AchievementEntityMapper;
import linc.com.alarmclockforprogrammers.data.mapper.QuestionEntityMapper;
import linc.com.alarmclockforprogrammers.data.preferences.LocalPreferencesManager;

import static linc.com.alarmclockforprogrammers.utils.Consts.ACHIEVEMENTS_LOCAL_VERSION;
import static linc.com.alarmclockforprogrammers.utils.Consts.ACHIEVEMENTS_REMOTE;
import static linc.com.alarmclockforprogrammers.utils.Consts.ACHIEVEMENTS_REMOTE_VERSION;
import static linc.com.alarmclockforprogrammers.utils.Consts.QUESTIONS_LOCAL_VERSION;
import static linc.com.alarmclockforprogrammers.utils.Consts.QUESTIONS_REMOTE;
import static linc.com.alarmclockforprogrammers.utils.Consts.QUESTIONS_REMOTE_VERSION;
import static linc.com.alarmclockforprogrammers.utils.Consts.THEME;

public class RepositoryVersionUpdate {

    private RemoteDatabase firebase;
    private QuestionsDao questionsDao;
    private AchievementsDao achievementsDao;
    private QuestionEntityMapper questionMapper;
    private AchievementEntityMapper achievementMapper;

    private LocalPreferencesManager preferences;

    public RepositoryVersionUpdate(RemoteDatabase firebase,
                                   QuestionsDao questionsDao,
                                   AchievementsDao achievementsDao,
                                   QuestionEntityMapper questionMapper,
                                   AchievementEntityMapper achievementMapper,
                                   LocalPreferencesManager preferences) {
        this.firebase = firebase;
        this.questionsDao = questionsDao;
        this.achievementsDao = achievementsDao;
        this.questionMapper = questionMapper;
        this.achievementMapper = achievementMapper;
        this.preferences = preferences;
    }

    public Completable updateLocalQuestionsVersion() {
        return Completable.create(emitter -> {
            Disposable d = firebase.getDataSnapshot(QUESTIONS_REMOTE_VERSION)
                    .subscribe(dataSnapshot -> {
                        String remoteVersion = ((String)dataSnapshot.getValue());
                        if(!remoteVersion.equals(preferences.getString(QUESTIONS_LOCAL_VERSION))
                                || questionsDao.getItemCount() == 0) {
                            Disposable questLocal = updateQuestions()
                                    .subscribe(() -> {
                                        preferences.saveString(remoteVersion, QUESTIONS_LOCAL_VERSION);
                                        emitter.onComplete();
                                    });
                            return;
                        }
                        emitter.onComplete();
                    }, emitter::onError);
        });
    }

    private Completable updateQuestions() {
        return Completable.create(emitter -> {
            Disposable d = firebase.getDataSnapshot(QUESTIONS_REMOTE)
                    .subscribe(dataSnapshot -> {
                        for(DataSnapshot ds : dataSnapshot.getChildren()) {
                            try {
                                questionsDao.insert(questionMapper.toQuestionEntity(ds));
                            }catch (Exception ignored) {}
                        }
                        emitter.onComplete();
                    }, emitter::onError);
        });
    }


    /**
     * Achievements
     */

    public Completable updateLocalAchievementsVersion() {
        return Completable.create(emitter -> {
            Disposable d = firebase.getDataSnapshot(ACHIEVEMENTS_REMOTE_VERSION)
                    .subscribe(dataSnapshot -> {
                        String remoteVersion = ((String)dataSnapshot.getValue());
                        if(!remoteVersion.equals(preferences.getString(ACHIEVEMENTS_LOCAL_VERSION))
                                || achievementsDao.getItemCount() == 0) {
                            Disposable achieveLocal = updateAchievements()
                                    .subscribe(() -> {
                                        preferences.saveString(remoteVersion, ACHIEVEMENTS_LOCAL_VERSION);
                                        emitter.onComplete();
                                    });
                            return;
                        }
                        emitter.onComplete();
                    }, emitter::onError);
        });
    }

    private Completable updateAchievements() {
        return Completable.create(emitter -> {
            Disposable d = firebase.getDataSnapshot(ACHIEVEMENTS_REMOTE)
                    .subscribe(dataSnapshot -> {
                        for(DataSnapshot ds : dataSnapshot.getChildren()) {
                            try {
                                achievementsDao.insert(achievementMapper.toAchievementEntity(ds));
                            }catch (Exception ignored) {}
                        }
                        emitter.onComplete();
                    }, emitter::onError);
        });
    }

    public Single<Boolean> getTheme() {
        return Single.fromCallable(() -> preferences.getBoolean(THEME));
    }
}