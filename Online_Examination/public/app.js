const questions=[
 {q:"Which language is primarily used to structure a web page?",o:["CSS","HTML","Java","SQL"],a:1},
 {q:"Which technology is used to style web pages?",o:["CSS","HTML","JDBC","Python"],a:0},
 {q:"Which keyword declares a variable in modern JavaScript?",o:["style","select","let","echo"],a:2},
 {q:"Which method converts JSON text into a JavaScript object?",o:["JSON.make()","JSON.parse()","JSON.read()","JSON.object()"],a:1},
 {q:"Which port is used by this demo server by default?",o:["21","25","443","8080"],a:3}
];
const $=id=>document.getElementById(id);
let seconds=300,clock=null,submitted=false;
$("loginForm").addEventListener("submit",async e=>{
 e.preventDefault();$("loginError").textContent="";
 try{
  const r=await fetch("/api/login",{method:"POST",headers:{"Content-Type":"application/json"},body:JSON.stringify({username:$("username").value.trim(),password:$("password").value})});
  const data=await r.json();if(!r.ok)throw new Error(data.message||"Login failed");
  startExam();
 }catch(err){$("loginError").textContent=err.message==="Failed to fetch"?"Cannot reach server. Please refresh and try again.":err.message;}
});
function startExam(){
 $("loginView").classList.add("hidden");$("resultView").classList.add("hidden");$("examView").classList.remove("hidden");
 $("questions").innerHTML=questions.map((q,i)=>`<article class="question"><div class="q-meta">QUESTION ${i+1} OF ${questions.length}</div><h3>${q.q}</h3>${q.o.map((v,j)=>`<label class="option"><input type="radio" name="q${i}" value="${j}"><span>${String.fromCharCode(65+j)}. ${v}</span></label>`).join("")}</article>`).join("");
 seconds=300;submitted=false;updateTimer();updateProgress();
 $("examForm").addEventListener("change",updateProgress);clearInterval(clock);clock=setInterval(()=>{seconds--;updateTimer();if(seconds<=0)finishExam();},1000);
}
function updateTimer(){const m=Math.floor(seconds/60),s=seconds%60;$("timer").textContent=`${String(m).padStart(2,"0")}:${String(s).padStart(2,"0")}`;}
function updateProgress(){const n=questions.filter((q,i)=>document.querySelector(`input[name=q${i}]:checked`)).length;$("progress").style.width=(n/questions.length*100)+"%";}
$("examForm").addEventListener("submit",e=>{e.preventDefault();finishExam();});
async function finishExam(){
 if(submitted)return;submitted=true;clearInterval(clock);
 const answers=questions.map((q,i)=>{const el=document.querySelector(`input[name=q${i}]:checked`);return el?Number(el.value):-1;});
 try{
  const r=await fetch("/api/submit",{method:"POST",headers:{"Content-Type":"application/json"},body:JSON.stringify({answers})});
  if(!r.ok)throw new Error("Could not calculate result");showResult(await r.json());
 }catch(err){alert(err.message);submitted=false;}
}
function showResult(data){
 $("examView").classList.add("hidden");$("resultView").classList.remove("hidden");
 $("score").textContent=`${data.score}/${data.total}`;$("percentage").textContent=data.percentage+"%";$("status").textContent=data.status;
 $("resultMessage").textContent=data.percentage===100?"Excellent work! You answered every question correctly.":"Your examination has been evaluated. Review the topics and try again.";
}
$("restart").addEventListener("click",()=>{$("resultView").classList.add("hidden");$("loginView").classList.remove("hidden");$("password").value="";});
