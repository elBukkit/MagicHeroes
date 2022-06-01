<?php

require_once('spyc.php');

// Don't allow from Apache
if (PHP_SAPI !== 'cli')
{
    die('Nope.');
}

if (count($argv) < 2) {
    die("Usage: generate.php <spell folder>\n");
}

error_reporting(E_ALL);
ini_set('display_errors', 1);

$spellConfigFolder = $argv[1];
$spellConfigs = array();

$spellFiles = new DirectoryIterator($spellConfigFolder);
foreach ($spellFiles as $spellFile) {
    if ($spellFile->isDir()) {continue;}
    if ($spellFile->getExtension() !== 'yml') {continue;}
    $spellConfigFile = $spellFile->getPathname();
    echo "Looking at $spellConfigFile\n";
    $spellConfig = spyc_load_file($spellConfigFile);
    foreach ($spellConfig as $key => $config) {
        $spellConfigs[$key] = $config;
        // Ignore spell levels
        break;
    }
}


$buildDir = 'build';
if (file_exists($buildDir)) {
    $files = new RecursiveIteratorIterator(
        new RecursiveDirectoryIterator($buildDir, RecursiveDirectoryIterator::SKIP_DOTS),
        RecursiveIteratorIterator::CHILD_FIRST
    );

    foreach ($files as $fileinfo) {
        $todo = ($fileinfo->isDir() ? 'rmdir' : 'unlink');
        $todo($fileinfo->getRealPath());
    }

    rmdir($buildDir);
}
mkdir($buildDir);

foreach ($spellConfigs as $spellKey => $spellConfig) {
    if (strpos($spellKey, '|') !== false) {continue;}
    if ($spellKey == 'default' || $spellKey == 'override') {continue;}
    if (isset($spellConfig['hidden']) && $spellConfig['hidden']) {continue;}
    if ((isset($spellConfig['category'])) && ($spellConfig['category'] == 'engineering'
    || $spellConfig['category'] == 'automata'
    || $spellConfig['category'] == 'shop'
    || $spellConfig['category'] == 'npc')) {continue;}
    if  (isset($spellConfig['inherit']) && (
        $spellConfig['inherit'] == 'buyshop'
        || $spellConfig['inherit'] == 'sellshop')) {continue;}
    echo "Generating skill for " . $spellKey . "\n";

    $source = 'src';
    $destRoot = 'build/' . $spellKey;
    mkdir($destRoot);
    $dest = $destRoot . '/src';
    mkdir($dest);
    copy('pom.xml', $destRoot . '/pom.xml');
    foreach (
        $iterator = new RecursiveIteratorIterator(
            new RecursiveDirectoryIterator($source, RecursiveDirectoryIterator::SKIP_DOTS),
            RecursiveIteratorIterator::SELF_FIRST) as $item) {
        if ($item->isDir()) {
            mkdir($dest . DIRECTORY_SEPARATOR . $iterator->getSubPathName());
        } else {
            copy($item, $dest . DIRECTORY_SEPARATOR . $iterator->getSubPathName());
        }
    }

    $className = "Skill" . $spellKey;
    $skillFileName = $dest . '/main/java/com/elmakers/mine/bukkit/heroes/skills/SkillTest.java';
    $skillFile = file_get_contents($skillFileName);
    unlink($skillFileName);
    $skillFile = str_replace('Test', $spellKey, $skillFile);
    $skillFile = str_replace('"test"', '"' . $spellKey . '"', $skillFile);
    $skillFileName = str_replace('Test', $spellKey, $skillFileName);
    file_put_contents($skillFileName, $skillFile);

    $infoFileName = $dest . '/main/resources/skill.info';
    $infoFile = file_get_contents($infoFileName);
    $infoFile = str_replace('Test', $spellKey, $infoFile);
    file_put_contents($infoFileName, $infoFile);

    chdir($destRoot);
    $jarName = "MagicSkill-" . $spellKey;
    system('mvn -Djar.finalName=' . $jarName . ' clean package');
    rename('target/' . $jarName . '.jar', '../../target/' . $jarName . '.jar');
    chdir('../..');
}