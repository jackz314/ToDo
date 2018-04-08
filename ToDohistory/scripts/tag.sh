#!/usr/bin/env bash
if [ "$TRAVIS_BRANCH" = "master" -a "$TRAVIS_PULL_REQUEST" = "false" ]; then
  git config --global user.email "zhangmengyu10@gmail.com"
  git config --global user.name "jackz314"

  git remote add release "https://${GH_TOKEN}@github.com/jackz314/ToDo.git"

  git push -d release debugdeploy
  git tag -d debugdeploy
  git tag -a "debugdeploy" -m "[AUTO DEPLOYED] This is auto-deployed by Travis CI for debug purposes."
  git push release --tags
fi